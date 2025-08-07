package com.om.integration.deal.om_api_integration.payload.request.refund.mtn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MtnCashoutRequestBody {
    private String customerkey;
    private String customersecret;
    private String webhook;
    private String amount;
    private String refund_method;
    private String final_customer_phone;
    private String final_customer_name;
    private String fees_included;
    private String payer_message;
}
