package com.om.integration.deal.om_api_integration.payload.request.refund.om;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusCheckRequest {
    @JsonProperty("customerkey")
    private String customerKey;
    @JsonProperty("customersecret")
    private String customerSecret;
}
