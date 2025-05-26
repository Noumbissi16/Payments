package com.om.integration.deal.om_api_integration.services.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.om.integration.deal.om_api_integration.model.Transaction;
import com.om.integration.deal.om_api_integration.model.ennum.RefundMethodEnum;
import com.om.integration.deal.om_api_integration.model.ennum.TransactionStatusEnum;
import com.om.integration.deal.om_api_integration.model.ennum.TransactionTypeEnum;
import com.om.integration.deal.om_api_integration.payload.request.refund.BalanceCheckRequest;
import com.om.integration.deal.om_api_integration.payload.request.refund.RefundRequest;
import com.om.integration.deal.om_api_integration.payload.response.payment.PaymentTokenResponse;
import com.om.integration.deal.om_api_integration.payload.request.refund.OmMakeRefundRequest;
import com.om.integration.deal.om_api_integration.model.RefundResponse;
import com.om.integration.deal.om_api_integration.payload.request.refund.StatusCheckRequest;
import com.om.integration.deal.om_api_integration.payload.response.refund.TransactionStatusResponse;
import com.om.integration.deal.om_api_integration.repository.RefundRepository;
import com.om.integration.deal.om_api_integration.repository.TransactionRepository;
import com.om.integration.deal.om_api_integration.services.RefundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@Slf4j
public class RefundServiceImpl implements RefundService {
    @Value( "${api.refund.url}" )
    String apiRefundUrl;
    @Value( "${api.refund.token.url}" )
    String apiRefundTokenUrl;
    @Value( "${api.channel.user.msisdn}" )
    String channelUserMsisdn;
    @Value( "${api.pin}" )
    String pin;
    @Value( "${api.refund.notification.url}" )
    String refundNotificationUrl;
    @Value("${api.refund.clientId}")
    String clientId;
    @Value("${api.refund.clientSecret}")
    String clientSecret;
    @Value( "${api.refund.customerKey}" )
    String customerKey;
    @Value( "${api.refund.customerSecret}" )
    String customerSecret;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    RefundRepository refundRepository;
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    ThreadPoolTaskScheduler taskScheduler;

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();


    @Override
    public ResponseEntity<?> getAccessTokenRefund() {
        String getAccessTokenUrl = apiRefundTokenUrl + "oauth2/token";
        String authorizationToEncode = clientId + ":" + clientSecret ;
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
            System.out.println("-----");
            log.error("Refund/Access token failed: {}", e.getResponseBodyAsString());
            try {
                // Try to parse known structured error body
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> errorMap = objectMapper.readValue(e.getResponseBodyAsString(), Map.class);
                return ResponseEntity.status(e.getStatusCode())
                        .body(errorMap);
            } catch (IOException parseEx) {
                // Fall back to raw string if parsing fails
                return ResponseEntity.status(e.getStatusCode())
                        .body(e.getResponseBodyAsString());
            }
        } catch (RestClientException e) {
            return ResponseEntity.internalServerError()
                    .body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> refundPayment(RefundRequest refundRequest) {
        String endpointUrl = apiRefundUrl + "/refund";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(refundRequest.getAccessToken());
        OmMakeRefundRequest request = OmMakeRefundRequest
                .builder()
                .customerkey(customerKey)
                .customersecret(customerSecret)
                .channelUserMsisdn(channelUserMsisdn)
                .pin(pin)
                .webhook(refundNotificationUrl)
                .amount(refundRequest.getAmount())
                .final_customer_phone(refundRequest.getCustomerPhoneNumber().trim())
                .final_customer_name(refundRequest.getCustomerName().trim())
                .refund_method(RefundMethodEnum.OrangeMoney.name())
                .fees_included("No")
                .final_cutomer_name_accuracy("50")
                .maximum_retries("9")
                .build();
        HttpEntity<OmMakeRefundRequest> requestEntity = new HttpEntity<>(request, headers);
        try {
            ResponseEntity<RefundResponse> response = restTemplate.postForEntity(
                    endpointUrl,
                    requestEntity,
                    RefundResponse.class
            );
            RefundResponse refundResponse = response.getBody();
            if (refundResponse == null || refundResponse.getMessageId() == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_GATEWAY)
                        .body("Refund initiated, but no transaction ID returned.");
            }
            refundRepository.save(refundResponse);

            Transaction transaction = Transaction.builder()
                    .messageId(refundResponse.getMessageId())
                    .amount(refundRequest.getAmount())
                    .payerNumber(refundRequest.getCustomerPhoneNumber())
                    .status(TransactionStatusEnum.PENDING.name())
                    .transactionType(TransactionTypeEnum.CASH_OUT)
                    .build();
            transactionRepository.save(transaction);

            this.scheduleRefundStatusCheck(refundRequest.getAccessToken(), refundResponse.getMessageId());

            return ResponseEntity.ok(refundResponse);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.out.println("-----");
            log.error("Refund/Deposit failed: {}", e.getResponseBodyAsString());
            try {
                // Try to parse known structured error body
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> errorMap = objectMapper.readValue(e.getResponseBodyAsString(), Map.class);

                // Normalize whitespace in the "body" field if present
                if (errorMap.containsKey("body") && errorMap.get("body") instanceof String) {
                    String cleanedBody = ((String) errorMap.get("body"))
                            .replaceAll("\\s+", " ")  // replace multiple spaces/tabs/newlines with a single space
                            .trim();                  // remove leading/trailing spaces
                    errorMap.put("body", cleanedBody);
                }

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(errorMap);
            } catch (IOException parseEx) {
                // Fall back to raw string if parsing fails
                return ResponseEntity.status(e.getStatusCode())
                        .body(e.getResponseBodyAsString());
            }
        } catch (RestClientException e) {
            System.out.println("-----");
            log.error("Refund/Deposit error: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body( e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> refundStatus(String accessToken, String refundTransactionId) {
        String statusUrl = apiRefundUrl + "/refund/status/" + refundTransactionId;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        StatusCheckRequest statusCheckRequest = StatusCheckRequest
                .builder()
                .customerKey(customerKey)
                .customerSecret(customerSecret)
                .build();

        HttpEntity<StatusCheckRequest> requestEntity =
                new HttpEntity<>(statusCheckRequest, headers);
        try {
            ResponseEntity<TransactionStatusResponse> response = restTemplate.exchange(
                    statusUrl,
                    HttpMethod.GET,
                    requestEntity,
                    TransactionStatusResponse.class
            );
            TransactionStatusResponse transactionStatusResponse = response.getBody();
            if (transactionStatusResponse == null || transactionStatusResponse.getResult() == null) {
                System.out.println("-----");
                log.warn("Empty refund status response received");
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Empty response from refund API");
            }
            TransactionStatusResponse.Result result = transactionStatusResponse.getResult();
            TransactionStatusResponse.Result.Dataa data = result.getData();
            TransactionStatusResponse.Parameters parameters = transactionStatusResponse.getParameters();

            if (isAllResultFieldsNull(result)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", Optional.ofNullable(result.getMessage())
                                .orElse("Refund failed with empty result fields")));
            }

            // Use data if present, else fall back to result
            String createtime = data != null ? data.getCreatetime() : result.getCreatetime();
            String payToken = data != null ? data.getPayToken() : result.getPayToken();
            String txnid = data != null ? data.getTxnid() : result.getTxnid();
            String txnmode = data != null ? data.getTxnmode() : result.getTxnmode();
            String txnstatus = data != null ? data.getTxnstatus() : result.getTxnstatus();
            String orderId = data != null ? data.getOrderId() : result.getOrderId();
            String status = data != null ? data.getStatus() : result.getStatus();
            String channelUserMsisdn = data != null ? data.getChannelUserMsisdn() : result.getChannelUserMsisdn();
            String fromChannelMsisdn = data != null ? data.getFromChannelMsisdn() : result.getFromChannelMsisdn();
            String description = data != null ? data.getDescription() : result.getDescription();

            System.out.println("message : " + transactionStatusResponse.getResult().getMessage());

            Transaction transaction;
            Optional<Transaction> optionalTransaction = transactionRepository.findByMessageId(refundTransactionId);

            if (optionalTransaction.isPresent()) {
                transaction = optionalTransaction.get();
                transaction.setRefundStep(transactionStatusResponse.getRefundStep());
                transaction.setStatus(status);
                transaction.setPayToken(payToken);
                transaction.setOmTransactionId(txnid);
                transaction.setOmTransactionDescription(txnmode);
                transaction.setInitTransactionStatus(txnstatus);
                transaction.setOrderId(orderId);
                transaction.setChannelUserMsisdn(channelUserMsisdn);
                transaction.setDescription(description);
                transaction.setFromChannelMsisdn(fromChannelMsisdn);
                transactionRepository.save(transaction);
            } else {
             transaction = Transaction.builder()
                    .createdTime(createtime)
                    .payToken(payToken)
                    .omTransactionId(txnid)
                    .omTransactionDescription(txnmode)
                    .initTransactionStatus(txnstatus)
                    .orderId(orderId)
                    .status(status)
                    .channelUserMsisdn(channelUserMsisdn)
                    .description(description)
                    .transactionType(TransactionTypeEnum.CASH_OUT)
                    .notificationUrl(refundNotificationUrl)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .messageId(refundTransactionId)
                    .refundStep(transactionStatusResponse.getRefundStep())
                     .fromChannelMsisdn(fromChannelMsisdn)
                    .build();
            transactionRepository.save(transaction);
            }
            return ResponseEntity.ok(transaction);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.out.println("-----");
            log.error("Status check failed: {}", e.getResponseBodyAsString());
            try {
                // Try to parse known structured error body
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> errorMap = objectMapper.readValue(e.getResponseBodyAsString(), Map.class);

                // Normalize whitespace in the "body" field if present
                if (errorMap.containsKey("body") && errorMap.get("body") instanceof String) {
                    String cleanedBody = ((String) errorMap.get("body"))
                            .replaceAll("\\s+", " ")  // replace multiple spaces/tabs/newlines with a single space
                            .trim();                  // remove leading/trailing spaces
                    errorMap.put("body", cleanedBody);
                }

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(errorMap);
            } catch (IOException parseEx) {
                // Fall back to raw string if parsing fails
                return ResponseEntity.status(e.getStatusCode())
                        .body(e.getResponseBodyAsString());
            }
//            return handleStatusError(e.getStatusCode(), e.getResponseBodyAsString());
        } catch (RestClientException e) {
            System.out.println("-----");
            log.error("RestClientException : Status check error: {}", e.getMessage());
            Throwable cause = e.getCause();
            boolean isJsonError = false;
            while (cause != null) {
                if (cause instanceof com.fasterxml.jackson.core.JsonParseException ||
                        cause instanceof org.springframework.http.converter.HttpMessageConversionException) {
                    isJsonError = true;
                    break;
                }
                cause = cause.getCause();
            }

            if (isJsonError) {
                // FALLBACK: re‑call expecting a plain String body
                try {
                    ResponseEntity<String> raw = restTemplate.exchange(
                            statusUrl,
                            HttpMethod.GET,
                            requestEntity,
                            String.class
                    );
                    String body = raw.getBody();
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                            .body(body);
                } catch (Exception ex) {
                    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                            .body("Unable to parse refund status response");
                }
            }
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    public void scheduleRefundStatusCheck(String accessToken, String refundTransactionId) {
        Runnable task = new Runnable() {
            private int attempt = 0;

            @Override
            public void run() {
                int maxAttempts = 90;
                System.out.println("🔁 [Refund Polling Attempt #" + (attempt + 1) + "] - Checking status for Transaction ID: " + refundTransactionId);

                if (attempt >= maxAttempts) {
                    System.out.println("⛔️ Max polling attempts reached for messageId: " + refundTransactionId);
                    ScheduledFuture<?> scheduledFuture = scheduledTasks.remove(refundTransactionId);
                    if (scheduledFuture != null) {
                        scheduledFuture.cancel(false);
                    }
                    return;
                }

                ResponseEntity<?> statusResponse = refundStatus(accessToken, refundTransactionId);
                if (statusResponse.getStatusCode().is2xxSuccessful() && statusResponse.getBody() instanceof Transaction transaction) {
                    String status = transaction.getStatus();
                    System.out.println("📦 Current refund status: " + status);

                    Optional<Transaction> optional = transactionRepository.findByMessageId(refundTransactionId);
                    if (optional.isPresent()) {
                        Transaction existing = optional.get();
                        existing.setStatus(status);
                        transactionRepository.save(existing);

                        if (!TransactionStatusEnum.PENDING.name().equalsIgnoreCase(status)) {
                            if (TransactionStatusEnum.SUCCESSFULL.name().equalsIgnoreCase(status)) {
                                if (!"1".equals(transaction.getRefundStep())) {
                                    System.out.println("✅ Final refund status: " + status + " — refundStep != 1, stopping polling for Transaction messageID: " + refundTransactionId);
                                    ScheduledFuture<?> scheduledFuture = scheduledTasks.remove(refundTransactionId);
                                    if (scheduledFuture != null) {
                                        scheduledFuture.cancel(false);
                                    }
                                } else {
                                    System.out.println("🔁 Refund is SUCCESSFULL but refundStep = 1 — keep polling for Transaction messageID: " + refundTransactionId);
                                    // Don't stop polling
                                }
                                return;
                            }
                            System.out.println("✅ Final refund status detected: " + status + " — polling stopped for Transaction messageID: " + refundTransactionId);

                            ScheduledFuture<?> scheduledFuture = scheduledTasks.remove(refundTransactionId);
                            if (scheduledFuture != null) {
                                scheduledFuture.cancel(false);
                            }
                            return;
                        }
                    }
                } else {
                    System.out.println("⚠️ Non-successful status response while polling for messageId: " + refundTransactionId);
                }

                attempt++;
            }
        };

        ScheduledFuture<?> future = taskScheduler.schedule(task, new PeriodicTrigger(Duration.ofSeconds(20)));
        scheduledTasks.put(refundTransactionId, future);
        System.out.println("🚀 Started polling for refund status — Transaction messageID: " + refundTransactionId);

    }


    private boolean isAllResultFieldsNull(TransactionStatusResponse.Result result) {
        return result.getData() == null
                && result.getCreatetime() == null
                && result.getSubscriberMsisdn() == null
                && result.getAmount() == null
                && result.getPayToken() == null
                && result.getTxnid() == null
                && result.getTxnmode() == null
                && result.getTxnstatus() == null
                && result.getOrderId() == null
                && result.getStatus() == null
                && result.getChannelUserMsisdn() == null
                && result.getDescription() == null
                && result.getFromChannelMsisdn() == null
                && result.getToChannelMsisdn() == null;
    }


    private ResponseEntity<?> handleStatusError(HttpStatusCode code, String body) {
        return switch (code.value()) {
            case 400 -> ResponseEntity.badRequest().body("Missing required parameters");
            case 401 -> ResponseEntity.status(401).body("Unauthorized - Invalid token");
            case 404 -> ResponseEntity.status(404).body("Transaction not found");
            case 5011 -> ResponseEntity.status(400).body("Invalid customer key");
            case 5012 -> ResponseEntity.status(400).body("Invalid customer secret");
            default -> ResponseEntity.status(code).body(body);
        };
    }

    @Override
    public ResponseEntity<?> accountBalance(String accessToken) {
        String balanceUrl = apiRefundUrl + "/balance";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest
                .builder()
                .customerKey(customerKey)
                .customerSecret(customerSecret)
                .paymentMethod("DEPOSIT")
                .build();

        HttpEntity<BalanceCheckRequest> requestEntity =
                new HttpEntity<>(balanceCheckRequest, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    balanceUrl,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            String responseBody = response.getBody();
            System.out.println("-----");
            log.debug("Balance check response: {}", responseBody);
            return ResponseEntity.ok(responseBody);
        } catch (HttpClientErrorException e) {
            System.out.println("-----");
            log.error("Balance check failed: {}", e.getResponseBodyAsString());
            return handleStatusError(e.getStatusCode(), e.getResponseBodyAsString());
        } catch (RestClientException e) {
            System.out.println("-----");
            log.error("Balance check error: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Balance check failed: " + e.getMessage());
        }
    }

}
