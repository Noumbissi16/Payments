package com.om.integration.deal.om_api_integration.payload.request.payment.mtn;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MtnPaymentUserRequest {
    private String phoneNumber;
    private String description;
    private String amount;
    private String orderId;
    private String accessToken;
}
