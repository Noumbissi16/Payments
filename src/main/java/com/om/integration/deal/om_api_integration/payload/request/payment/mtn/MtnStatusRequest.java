package com.om.integration.deal.om_api_integration.payload.request.payment.mtn;

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
public class MtnStatusRequest {
    @JsonProperty("message_id")
    private String messageId;
    @JsonProperty("customerkey")
    private String customerKey;
    @JsonProperty("customersecret")
    private String customerSecret;
    @JsonProperty("authToken")
    private String authToken;

}
