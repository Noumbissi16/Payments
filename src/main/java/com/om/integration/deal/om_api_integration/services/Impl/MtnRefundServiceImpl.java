package com.om.integration.deal.om_api_integration.services.Impl;

import com.om.integration.deal.om_api_integration.model.Transaction;
import com.om.integration.deal.om_api_integration.model.ennum.TransactionStatusEnum;
import com.om.integration.deal.om_api_integration.model.ennum.TransactionTypeEnum;
import com.om.integration.deal.om_api_integration.payload.request.refund.mtn.MtnCashoutRequest;
import com.om.integration.deal.om_api_integration.payload.request.refund.mtn.MtnCashoutStatusRequest;
import com.om.integration.deal.om_api_integration.payload.request.refund.om.RefundRequest;
import com.om.integration.deal.om_api_integration.payload.response.payment.mtn.MtnTokenResponse;
import com.om.integration.deal.om_api_integration.payload.response.payment.mtn.MtnTransactionStatusResponse;
import com.om.integration.deal.om_api_integration.payload.response.refund.mtn.MtnCashoutResponse;
import com.om.integration.deal.om_api_integration.repository.TransactionRepository;
import com.om.integration.deal.om_api_integration.services.MtnRefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;


@Service
public class MtnRefundServiceImpl implements MtnRefundService {

    @Value("${api.mtn.clientId}")
    private String mtnClientId;

    @Value("${api.mtn.clientSecret}")
    private String mtnClientSecret;

    @Value("${api.mtn.customerKey}")
    private String mtnCustomerKey;

    @Value("${api.mtn.customerSecret}")
    private String mtnCustomerSecret;

    @Value("${mtn.api.cashout.url}")
    private String cashoutUrl;

    @Value("${mtn.api.cashout.status.url}")
    private String cashoutStatusUrl;

    @Value("${mtn.api.status.url}")
    private String statusUrl;

    @Value("${mtn.api.token.url}")
    private String tokenUrl;

    @Value( "${api.notification.url}" )
    String notificationUrl;

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    ThreadPoolTaskScheduler taskScheduler;

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();


    @Override
    public ResponseEntity<MtnCashoutResponse> initiateCashout(MtnCashoutRequest cashoutRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(cashoutRequest.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        MtnCashoutRequest request = MtnCashoutRequest
                .builder()
                .customerKey(mtnCustomerKey)
                .customerSecret(mtnCustomerSecret)
                .amount(cashoutRequest.getAmount())
                .finalCustomerPhone(cashoutRequest.getFinalCustomerPhone())
                .finalCustomerName(cashoutRequest.getFinalCustomerName())
                .webhook(notificationUrl)
                .refundMethod("MTN_MOMO_CMR")
                .feesIncluded("No")
                .payerMessage(cashoutRequest.getPayerMessage())
                .build();

        HttpEntity<MtnCashoutRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<MtnCashoutResponse> response = restTemplate.postForEntity(
                    cashoutUrl,
                    entity,
                    MtnCashoutResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Save transaction with MessageId
                Transaction transaction = Transaction.builder()
                        .transactionType(TransactionTypeEnum.CASH_OUT)
                        .status(TransactionStatusEnum.INITIATED.name())
                        .omTransactionId(response.getBody().getMessageId())
                        .amount(cashoutRequest.getAmount())
                        .payerNumber(cashoutRequest.getFinalCustomerPhone())
                        .build();
                transactionRepository.save(transaction);

                // Start polling for refund status
                scheduleCashoutStatusCheck(cashoutRequest.getAccessToken(), response.getBody().getMessageId());

                return ResponseEntity.ok(response.getBody());
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(MtnCashoutResponse.builder().errorMessage("Cashout failed").build());

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(MtnCashoutResponse.builder().errorMessage(e.getResponseBodyAsString()).build());
        } catch (RestClientException e) {
            return ResponseEntity.internalServerError()
                    .body(MtnCashoutResponse.builder().errorMessage(e.getMessage()).build());
        }

    }

    @Override
    public ResponseEntity<MtnTransactionStatusResponse> getCashoutStatus(
            String messageId, String accessToken
    ) {

        String url = cashoutStatusUrl + messageId;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        MtnCashoutStatusRequest build = MtnCashoutStatusRequest
                .builder()
                .refundMethod("MTN_MOMO_CMR")
                .customerKey(mtnCustomerKey)
                .customerSecret(mtnCustomerSecret)
                .build();

        HttpEntity<MtnCashoutStatusRequest> entity = new HttpEntity<>(build, headers);

        try {
            // ✅ Use exchange because GET with body is unusual, but some APIs allow it
            ResponseEntity<MtnTransactionStatusResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    MtnTransactionStatusResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response;
            }
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

        } catch (HttpClientErrorException e) {
            System.out.println("❌ [CashoutStatus] HttpClientErrorException: " + e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode())
                    .body(MtnTransactionStatusResponse.builder().errorMessage(e.getResponseBodyAsString()).build());
        } catch (RestClientException e) {
            System.out.println("❌ [CashoutStatus] RestClientException: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(MtnTransactionStatusResponse.builder().errorMessage(e.getMessage()).build());
        }

    }

    @Override
    public ResponseEntity<MtnTokenResponse> getAccessTokenRequestMTN() {
        String authorizationToEncode = mtnClientId + ":" + mtnClientSecret ;
        String encodedAuth = Base64.getEncoder().encodeToString(authorizationToEncode.getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(encodedAuth);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<MtnTokenResponse> response = restTemplate.postForEntity(
                    tokenUrl,
                    requestEntity,
                    MtnTokenResponse.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new MtnTokenResponse(null, null,0, e.getResponseBodyAsString()));
        } catch (RestClientException e) {
            return ResponseEntity.internalServerError()
                    .body(new MtnTokenResponse(null, null,0, "Error fetching token: " + e.getMessage()));
        }
    }
    public void scheduleCashoutStatusCheck(String initialToken, String messageId) {
        Runnable task = new Runnable() {
            private int attempt = 0;
            private String token = initialToken;

            @Override
            public void run() {
                System.out.println("🔄 [Cashout Polling] Attempt #" + (attempt + 1) + " for refund MessageId: " + messageId);

                if (attempt >= 90) { // 30 mins
                    cancelTask(messageId);
                    return;
                }

                ResponseEntity<MtnTransactionStatusResponse> response = getCashoutStatus(messageId, token);

                if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    System.out.println("⚠️ [Cashout Polling] Token expired, refreshing...");
                    try {
                        ResponseEntity<MtnTokenResponse> tokenResponse = getAccessTokenRequestMTN();
                        if (tokenResponse.getStatusCode().is2xxSuccessful() && tokenResponse.getBody() != null) {
                            token = tokenResponse.getBody().getAccessToken();
                        } else {
                            cancelTask(messageId);
                            return;
                        }
                    } catch (Exception e) {
                        cancelTask(messageId);
                        return;
                    }
                    attempt++;
                    return;
                }

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    String status = response.getBody().getStatus();
                    if (status != null) {
                        Optional<Transaction> optTx = transactionRepository.findByOmTransactionId(messageId);
                        optTx.ifPresent(tx -> {
                            tx.setStatus(status);
                            transactionRepository.save(tx);
                        });

                        if (!TransactionStatusEnum.PENDING.name().equalsIgnoreCase(status)) {
                            System.out.println("✅ [Cashout Polling] Refund completed with status: " + status);
                            cancelTask(messageId);
                            return;
                        }
                    }
                }

                attempt++;
            }

            private void cancelTask(String id) {
                ScheduledFuture<?> future = scheduledTasks.remove(id);
                if (future != null) {
                    future.cancel(false);
                }
                System.out.println("⏹️ [Cashout Polling] Stopped polling for refund MessageId: " + id);
            }
        };

        ScheduledFuture<?> future = taskScheduler.schedule(task, new PeriodicTrigger(Duration.ofSeconds(20)));
        scheduledTasks.put(messageId, future);
    }



}
