package com.om.integration.deal.om_api_integration.payload.request.payment.mtn;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MtnBalanceRequest {
    private String customerkey;
    private String customersecret;
    private String payment_method;
}
