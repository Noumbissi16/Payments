package com.om.integration.deal.om_api_integration.payload.request.refund.om;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OmMakeRefundRequest {
    private String customerkey;
    private String customersecret;
    private String channelUserMsisdn;
    private String pin;
    private String webhook;
    private String amount;
    private String final_customer_phone;
    private String final_customer_name;
    private String refund_method;
    private String fees_included;
    private String final_cutomer_name_accuracy;
    private String maximum_retries;
}
