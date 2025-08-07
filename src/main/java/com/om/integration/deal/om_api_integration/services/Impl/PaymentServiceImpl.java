package com.om.integration.deal.om_api_integration.services.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.om.integration.deal.om_api_integration.model.Transaction;
import com.om.integration.deal.om_api_integration.model.ennum.TransactionStatusEnum;
import com.om.integration.deal.om_api_integration.model.ennum.TransactionTypeEnum;
import com.om.integration.deal.om_api_integration.payload.request.payment.om.NotifUrlRequestBody;
import com.om.integration.deal.om_api_integration.payload.request.payment.om.OmMakePaymentRequest;
import com.om.integration.deal.om_api_integration.payload.request.payment.om.PaymentRequest;
import com.om.integration.deal.om_api_integration.payload.response.payment.om.InitiatePaymentResponse;
import com.om.integration.deal.om_api_integration.payload.response.payment.om.MakePaymentResponse;
import com.om.integration.deal.om_api_integration.payload.response.payment.om.PaymentTokenResponse;
import com.om.integration.deal.om_api_integration.repository.TransactionRepository;
import com.om.integration.deal.om_api_integration.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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
public class PaymentServiceImpl implements PaymentService {

    @Value( "${api.url}" )
    String apiUrl;
    @Value( "${api.x.auth.token}" )
    String X_AUTH_TOKEN;
    @Value( "${api.username}" )
    String username;
    @Value( "${api.mot.de.passe}" )
    String mot_de_passe;
    @Value( "${api.channel.user.msisdn}" )
    String channelUserMsisdn;
    @Value( "${api.pin}" )
    String pin;
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
    public ResponseEntity<?> getAccessTokenRequest() {
        String getAccessTokenUrl = apiUrl + "token";
        String authorizationToEncode = username + ":" + mot_de_passe ;
        String encodedAuth = Base64.getEncoder().encodeToString(authorizationToEncode.getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(encodedAuth);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<PaymentTokenResponse> response = restTemplate.postForEntity(
                    getAccessTokenUrl,
                    requestEntity,
                    PaymentTokenResponse.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());
        } catch (RestClientException e) {
            return ResponseEntity.internalServerError()
                    .body("Error fetching token: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> initiatePayment(String accessToken) {
        String initiatePaymentUrl = apiUrl + "omcoreapis/1.0.2/mp/init";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-AUTH-TOKEN",X_AUTH_TOKEN);
        headers.setBearerAuth(accessToken);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try{
            ResponseEntity<InitiatePaymentResponse> response = restTemplate.postForEntity(initiatePaymentUrl, requestEntity, InitiatePaymentResponse.class);
            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body("Initiate Payment Failed: " + e.getMessage());
        } catch (RestClientException e) {
            return ResponseEntity.internalServerError()
                    .body("Initiate Payment Error: " + e.getMessage());
        }

    }

    @Override
    public ResponseEntity<?> makePayment(PaymentRequest paymentRequest) {
        String makePaymentUrl = apiUrl + "omcoreapis/1.0.2/mp/pay";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-AUTH-TOKEN",X_AUTH_TOKEN);
        headers.setBearerAuth(paymentRequest.getAccessToken());
        OmMakePaymentRequest makePaymentRequestBody = OmMakePaymentRequest
                .builder()
                .notifUrl(notificationUrl)
                .channelUserMsisdn(channelUserMsisdn)
                .amount(paymentRequest.getAmount())
                .subscriberMsisdn(paymentRequest.getPhoneNumber())
                .pin(pin)
                .orderId(paymentRequest.getOrderId())
                .description(paymentRequest.getDescription())
                .payToken(paymentRequest.getPaymentToken())
                .build();
        HttpEntity<OmMakePaymentRequest> requestEntity = new HttpEntity<>(makePaymentRequestBody, headers);
        try{
            ResponseEntity<MakePaymentResponse> response = restTemplate.postForEntity(makePaymentUrl, requestEntity, MakePaymentResponse.class);
            if(response.getBody() == null){
                return ResponseEntity.internalServerError()
                        .body("Payment Processing Error: " + "Response Body is null");
            }
            MakePaymentResponse.PaymentData paymentResponseData = response.getBody().getData();
            System.out.println("paymentResponseData = " + paymentResponseData);
            Transaction transaction = Transaction.builder()
                    .confirmTransactionStatus(paymentResponseData.getConfirmtxnstatus())
                    .createdTime(paymentResponseData.getCreatetime())
                    .createdAt(Instant.now())
                    .passCode(paymentResponseData.getPassCode())
                    .notificationUrl(paymentResponseData.getNotifUrl())
                    .omTransactionId(paymentResponseData.getTxnid())
                    .payerNumber(paymentResponseData.getChannelUserMsisdn())
                    .amount(paymentResponseData.getAmount())
                    .payToken(paymentResponseData.getPayToken())
                    .omTransactionDescription(paymentResponseData.getTxnmode())
                    .omInitTransactionMessage(paymentResponseData.getInittxnmessage())
                    .confirmTransactionMessage(paymentResponseData.getConfirmtxnmessage())
                    .confirmTransactionStatus(paymentResponseData.getConfirmtxnstatus())
                    .initTransactionStatus(paymentResponseData.getInittxnstatus())
                    .orderId(paymentResponseData.getOrderId())
                    .status(paymentResponseData.getStatus())
                    .updatedAt(Instant.now())
                    .build();
            transactionRepository.save(transaction);
            this.schedulePaymentStatusCheck(paymentRequest.getAccessToken(), paymentResponseData.getPayToken());
            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException e) {
            System.out.println(" Payment Failed: " + e.getMessage());
            // Extract and store failed response data if available
            System.out.println("e.getResponseBodyAsString() = " + e.getResponseBodyAsString());
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                MakePaymentResponse failedResponse = objectMapper.readValue(e.getResponseBodyAsString(), MakePaymentResponse.class);
                MakePaymentResponse.PaymentData failedData = failedResponse.getData();

                Transaction failedTransaction = Transaction.builder()
                        .confirmTransactionStatus(failedData.getConfirmtxnstatus())
                        .createdTime(failedData.getCreatetime())
                        .createdAt(Instant.now())
                        .passCode(failedData.getPassCode())
                        .notificationUrl(failedData.getNotifUrl())
                        .omTransactionId(failedData.getTxnid())
                        .payerNumber(failedData.getChannelUserMsisdn())
                        .amount(failedData.getAmount())
                        .payToken(failedData.getPayToken())
                        .omTransactionDescription(failedData.getTxnmode())
                        .omInitTransactionMessage(failedData.getInittxnmessage())
                        .confirmTransactionMessage(failedData.getConfirmtxnmessage())
                        .confirmTransactionStatus(failedData.getConfirmtxnstatus())
                        .initTransactionStatus(failedData.getInittxnstatus())
                        .orderId(failedData.getOrderId())
                        .status(failedData.getStatus())
                        .transactionType(TransactionTypeEnum.CASH_IN)
                        .build();
                transactionRepository.save(failedTransaction);
            } catch (Exception parseException) {
                System.out.println("Failed to parse error response: " + parseException.getMessage());
            }
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());
        } catch (RestClientException e) {
            System.out.println(" Payment Error: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Payment Processing Error: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> getPaymentStatus(String accessToken,String paymentToken) {
        String makePaymentUrl = apiUrl + "omcoreapis/1.0.2/mp/paymentstatus/" + paymentToken;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-AUTH-TOKEN",X_AUTH_TOKEN);
        headers.setBearerAuth(accessToken);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(headers);
        try{
            ResponseEntity<MakePaymentResponse> response = restTemplate.exchange(makePaymentUrl, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<MakePaymentResponse>() {
            });
            String status = Objects.requireNonNull(response.getBody()).getData().getStatus();
            Optional<Transaction> optionalTransaction = transactionRepository.findByPayToken(paymentToken);
            if (optionalTransaction.isPresent()) {
                Transaction transaction = optionalTransaction.get();
                if (transaction.getStatus().equalsIgnoreCase(status)) {
                    return ResponseEntity.ok(status);
                }
                transaction.setStatus(status);
                transactionRepository.save(transaction);
            }
            return ResponseEntity.ok(status);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        } catch (RestClientException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @Override
    public ResponseEntity<?> ntsCashinNotifUrl(NotifUrlRequestBody notifUrlRequestBody) {
        Optional<Transaction> optionalTransaction = transactionRepository.findByPayToken(notifUrlRequestBody.getPayToken());
        if (optionalTransaction.isEmpty()){
            return ResponseEntity.ok().build();
        }
        Transaction transaction = optionalTransaction.get();
        if (transaction.getStatus().equalsIgnoreCase(notifUrlRequestBody.getStatus())) {
            return ResponseEntity.ok().build();
        }
        transaction.setStatus(notifUrlRequestBody.getStatus());
        transactionRepository.save(transaction);
        return ResponseEntity.ok().build();
    }


    public void schedulePaymentStatusCheck(String accessToken, String paymentToken) {
        Runnable task = new Runnable() {
            private int attempt = 0;
            @Override
            public void run() {
                // 30 minutes (90 requests)
                int maxAttempts = 90;
                if(attempt >= maxAttempts) {
                    ScheduledFuture<?> scheduledFuture = scheduledTasks.remove(paymentToken);
                    if (scheduledFuture != null) {
                        scheduledFuture.cancel(false);
                    }
                    return;
                }
                ResponseEntity<String> response = getPaymentStatus(accessToken, paymentToken);
                if(response.getStatusCode().is2xxSuccessful()){
                    String status = response.getBody();
                    if(status != null){
                        Optional<Transaction> optionalTransaction = transactionRepository.findByPayToken(paymentToken);
                        if(optionalTransaction.isPresent()){
                            Transaction transaction = optionalTransaction.get();
                            transaction.setStatus(status);
                            transactionRepository.save(transaction);
                            if(!TransactionStatusEnum.PENDING.name().equalsIgnoreCase(status)){
                                ScheduledFuture<?> scheduledFuture = scheduledTasks.remove(paymentToken);
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
        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(task, new PeriodicTrigger(Duration.ofSeconds(20)));
        scheduledTasks.put(paymentToken, scheduledFuture);
    }


}
