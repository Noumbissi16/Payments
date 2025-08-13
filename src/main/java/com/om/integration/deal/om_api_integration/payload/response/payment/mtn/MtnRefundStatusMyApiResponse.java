package com.om.integration.deal.om_api_integration.payload.response.payment.mtn;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MtnRefundStatusMyApiResponse {
    private String omTransactionId;
    private String status;
}
