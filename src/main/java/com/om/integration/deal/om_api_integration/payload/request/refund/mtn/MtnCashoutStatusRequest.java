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
public class MtnCashoutStatusRequest {
    @JsonProperty("customerkey")
    private String customerKey;
    @JsonProperty("customersecret")
    private String customerSecret;
    @JsonProperty("accessToken")
    private String accessToken;
    @JsonProperty("messageId")
    private String messageId;
    @JsonProperty("refund_method")
    private String refundMethod;
}
