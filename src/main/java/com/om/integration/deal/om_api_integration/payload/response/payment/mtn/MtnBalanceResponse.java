package com.om.integration.deal.om_api_integration.payload.response.payment.mtn;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MtnBalanceResponse {
    private String balance;
    private String value;
    private String errorMessage;
}
