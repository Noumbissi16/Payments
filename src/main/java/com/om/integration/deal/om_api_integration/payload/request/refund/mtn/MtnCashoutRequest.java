package com.om.integration.deal.om_api_integration.payload.request.refund.mtn;


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
public class MtnCashoutRequest {
    @JsonProperty("customerkey")
    private String customerKey;
    @JsonProperty("customersecret")
    private String customerSecret;
    private String webhook;
    private String amount;
    @JsonProperty("refund_method")
    private String refundMethod; // e.g. "MTN_MOMO_CMR"
    @JsonProperty("final_customer_phone")
    private String finalCustomerPhone;
    @JsonProperty("final_customer_name")
    private String finalCustomerName;
    @JsonProperty("fees_included")
    private String feesIncluded; // "Yes" or "No"
    @JsonProperty("payer_message")
    private String payerMessage;
    @JsonProperty("accessToken")
    private String accessToken;
}
