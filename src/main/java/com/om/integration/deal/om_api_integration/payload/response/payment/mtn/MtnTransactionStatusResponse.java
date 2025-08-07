package com.om.integration.deal.om_api_integration.payload.response.payment.mtn;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MtnTransactionStatusResponse {
    private String status;
    private Parameters parameters;
    private String errorMessage;
    private int statusCode;
    private String body;
    private String createAt;
    private String messageId;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Parameters {
        private String operation;
        private String currency;
        private String amount;
        private String referenceId;
        @JsonProperty("final_customer_phone")
        private String finalCustomerPhone;
        @JsonProperty("final_customer_name")
        private String finalCustomerName;
        private String notifUrl;
        private String MessageId;
    }
}
