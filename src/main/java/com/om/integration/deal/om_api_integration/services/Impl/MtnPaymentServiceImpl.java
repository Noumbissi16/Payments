package com.om.integration.deal.om_api_integration.services.Impl;

import com.om.integration.deal.om_api_integration.model.Transaction;
import com.om.integration.deal.om_api_integration.model.ennum.TransactionStatusEnum;
import com.om.integration.deal.om_api_integration.model.ennum.TransactionTypeEnum;
import com.om.integration.deal.om_api_integration.payload.request.payment.mtn.MTNMakePaymentRequestApiMut;
import com.om.integration.deal.om_api_integration.payload.request.payment.mtn.MTNMakePaymentRequestBody;
import com.om.integration.deal.om_api_integration.payload.request.payment.mtn.MtnPaymentUserRequest;
import com.om.integration.deal.om_api_integration.payload.request.payment.mtn.MtnStatusRequest;
import com.om.integration.deal.om_api_integration.payload.response.payment.mtn.MtnMakePaymentResponse;
import com.om.integration.deal.om_api_integration.payload.response.payment.mtn.MtnTokenResponse;
import com.om.integration.deal.om_api_integration.payload.response.payment.mtn.MtnTransactionStatusResponse;
import com.om.integration.deal.om_api_integration.repository.TransactionRepository;
import com.om.integration.deal.om_api_integration.services.MtnPaymentService;
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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;


@Service
public class MtnPaymentServiceImpl implements MtnPaymentService {

//    @Value("${api.mtn.clientId}")
//    private String mtnClientId;

    @Value("${api.mtn.clientSecret}")
    private String mtnClientSecret;

    @Value("${api.mtn.customerKey}")
    private String mtnCustomerKey;

    @Value("${api.mtn.customerSecret}")
    private String mtnCustomerSecret;

    @Value("${mtn.api.token.url}")
    private String tokenUrl;

    @Value("${mtn.api.collection.url}")
    private String collectionUrl;

    @Value("${mtn.api.status.url}")
    private String statusUrl;

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
    public ResponseEntity<MtnTokenResponse> getAccessTokenRequestMTN() {
        String authorizationToEncode = "mtnClientId" + ":" + mtnClientSecret ;
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

    @Override
    public ResponseEntity<MtnMakePaymentResponse> makePaymentMTN(MtnPaymentUserRequest mtnPaymentUserRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(mtnPaymentUserRequest.getAccessToken());
        MTNMakePaymentRequestApiMut mtnMakePaymentRequestApiMut = MTNMakePaymentRequestApiMut.builder()
                .notifUrl(notificationUrl)
                .subscriberMsisdn(mtnPaymentUserRequest.getPhoneNumber())
                .description(mtnPaymentUserRequest.getDescription())
                .amount(mtnPaymentUserRequest.getAmount())
                .orderId(mtnPaymentUserRequest.getOrderId())
                .customerSecret(mtnCustomerSecret)
                .customerKey(mtnCustomerKey)
                .paiementMethod("MTN_CMR")
                .build();
        MTNMakePaymentRequestBody mtnMakePaymentRequestBody = MTNMakePaymentRequestBody
                .builder()
                .API_MUT(mtnMakePaymentRequestApiMut)
                .build();
        HttpEntity<MTNMakePaymentRequestBody> requestEntity = new HttpEntity<>(mtnMakePaymentRequestBody, headers);
        try {
            ResponseEntity<MtnMakePaymentResponse> response = restTemplate.postForEntity(
                    collectionUrl,
                    requestEntity,
                    MtnMakePaymentResponse.class
            );
            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && response.getBody().getErrorCode() == 200
            ) {
                Transaction transaction = Transaction.builder()
                        .amount(mtnPaymentUserRequest.getAmount())
                        .payerNumber(mtnPaymentUserRequest.getPhoneNumber())
                        .orderId(mtnPaymentUserRequest.getOrderId())
                        .status(TransactionStatusEnum.INITIATED.name())
                        .transactionType(TransactionTypeEnum.CASH_IN)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .omTransactionId(response.getBody().getParameters().getMessageId())
                        .payToken(response.getBody().getParameters().getMessageId())
                        .notificationUrl(notificationUrl)
                        .build();

                transactionRepository.save(transaction);

                this.schedulePaymentStatusCheckMTN(mtnPaymentUserRequest.getAccessToken(), response.getBody().getParameters().getMessageId());
            }

            return ResponseEntity.ok(response.getBody());

        } catch (HttpClientErrorException e) {
            System.out.println("Raw response: " + e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode())
                    .body(
                            MtnMakePaymentResponse
                                    .builder()
                                    .errorMessage(e.getResponseBodyAsString())
                                    .build()
                    );
        } catch (RestClientException e) {
            return ResponseEntity.internalServerError()
                    .body(MtnMakePaymentResponse
                            .builder()
                            .errorMessage(e.getMessage())
                            .build()
                    );

        }

    }

    @Override
    public ResponseEntity<?> getPaymentStatusMTN(String messageId, String authToken) {
        try {
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authToken);

            // Create request body
            MtnStatusRequest requestBody = MtnStatusRequest
                    .builder()
                    .customerSecret(mtnCustomerSecret)
                    .customerKey(mtnCustomerKey)
                    .messageId(messageId)
                    .build();

            // Create request entity
            HttpEntity<MtnStatusRequest> requestEntity = new HttpEntity<>(requestBody, headers);

            // Make request
            ResponseEntity<MtnTransactionStatusResponse> response = restTemplate.postForEntity(
                    statusUrl,
                    requestEntity,
                    MtnTransactionStatusResponse.class
            );

            // Update transaction status if found
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Optional<Transaction> optionalTransaction = transactionRepository.findByPayToken(messageId);
                if (optionalTransaction.isPresent()) {
                    Transaction transaction = optionalTransaction.get();
                    transaction.setStatus(response.getBody().getStatus());
                    transactionRepository.save(transaction);
                }
            }

            String status = Objects.requireNonNull(response.getBody()).getStatus();
            return ResponseEntity.ok(status);

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(
                            MtnTransactionStatusResponse.builder().errorMessage(e.getResponseBodyAsString()).build()
                    );
        } catch (RestClientException e) {
            return ResponseEntity.internalServerError()
                    .body( MtnTransactionStatusResponse.builder().errorMessage(e.getMessage()).build());
        }
    }

    public void schedulePaymentStatusCheckMTN(String initialAccessToken, String messageId) {
        Runnable task = new Runnable() {
            private int attempt = 0;
            private String token = initialAccessToken; // start with the token used during payment

            @Override
            public void run() {
                System.out.println("🔄 [MTN Polling] Attempt #" + (attempt + 1) + " for MessageId: " + messageId);

                int maxAttempts = 90; // 30 minutes @ 20s interval
                if (attempt >= maxAttempts) {
                    System.out.println("⏹️ [MTN Polling] Max attempts reached. Stopping polling for MessageId: " + messageId);
                    ScheduledFuture<?> scheduledFuture = scheduledTasks.remove(messageId);
                    if (scheduledFuture != null) {
                        scheduledFuture.cancel(false);
                    }
                    return;
                }

                // Call getPaymentStatusMTN
                ResponseEntity<?> response = getPaymentStatusMTN(messageId, token);
                System.out.println("📥 [MTN Polling] Response received: " + response.getStatusCode());

                // ✅ Handle 401 Unauthorized → Refresh token
                if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    try {
                        ResponseEntity<MtnTokenResponse> tokenResponse = getAccessTokenRequestMTN();
                        if (tokenResponse.getStatusCode().is2xxSuccessful() && tokenResponse.getBody() != null) {
                            token = tokenResponse.getBody().getAccessToken(); // <-- use new token
                            System.out.println("✅ Refreshed MTN access token successfully");
                        } else {
                            System.out.println("❌ Failed to refresh MTN token, stopping scheduler");
                            ScheduledFuture<?> scheduledFuture = scheduledTasks.remove(messageId);
                            if (scheduledFuture != null) {
                                scheduledFuture.cancel(false);
                            }
                            return;
                        }
                    } catch (Exception e) {
                        System.out.println("❌ Exception while refreshing MTN token: " + e.getMessage());
                        ScheduledFuture<?> scheduledFuture = scheduledTasks.remove(messageId);
                        if (scheduledFuture != null) {
                            scheduledFuture.cancel(false);
                        }
                        return;
                    }
                    attempt++;
                    return; // retry in next scheduled execution
                }

                // ✅ If successful, parse response
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() instanceof MtnTransactionStatusResponse statusResponse) {
                    String status = statusResponse.getStatus();
                    System.out.println("📌 [MTN Polling] Transaction status received: " + status);
                    if (status != null) {
                        Optional<Transaction> optionalTransaction = transactionRepository.findByPayToken(messageId);
                        if (optionalTransaction.isPresent()) {
                            Transaction transaction = optionalTransaction.get();
                            transaction.setStatus(status);
                            transactionRepository.save(transaction);

                            // ✅ Stop polling when transaction is no longer PENDING
                            if (!TransactionStatusEnum.PENDING.name().equalsIgnoreCase(status)) {
                                System.out.println("✅ [MTN Polling] Transaction final status reached (" + status + "). Stopping polling.");
                                ScheduledFuture<?> scheduledFuture = scheduledTasks.remove(messageId);
                                if (scheduledFuture != null) {
                                    scheduledFuture.cancel(false);
                                }
                                return;
                            }
                        }
                    }
                }

                attempt++;
            }
        };

        System.out.println("🚀 [MTN Polling] Starting polling for MessageId: " + messageId + " every 20 seconds.");
        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(task, new PeriodicTrigger(Duration.ofSeconds(20)));
        scheduledTasks.put(messageId, scheduledFuture);
    }


}
