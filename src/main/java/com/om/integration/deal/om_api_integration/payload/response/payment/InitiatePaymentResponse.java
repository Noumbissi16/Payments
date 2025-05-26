package com.om.integration.deal.om_api_integration.payload.response.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InitiatePaymentResponse {
    private String message;

    private PaymentData data;

    @Data
    public static class PaymentData {
        @JsonProperty("payToken")
        private String payToken;
    }
}
