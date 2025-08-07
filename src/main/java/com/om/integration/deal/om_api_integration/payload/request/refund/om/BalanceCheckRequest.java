package com.om.integration.deal.om_api_integration.payload.request.refund.om;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BalanceCheckRequest {
    @JsonProperty("customerkey")
    private String customerKey;
    @JsonProperty("customersecret")
    private String customerSecret;
    @JsonProperty("payment_method")
    private String paymentMethod = "DEPOSIT";
}
