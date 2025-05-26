package com.om.integration.deal.om_api_integration.payload.response.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MakePaymentResponse {
    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private PaymentData data;

    @Data
    public static class PaymentData {

        @JsonProperty("id")
        private Long id;

        @JsonProperty("createtime")
        private String createtime;

        @JsonProperty("passCode")
        private String passCode;

        @JsonProperty("subscriberMsisdn")
        private String subscriberMsisdn;

        @JsonProperty("amount")
        private String amount;

        @JsonProperty("payToken")
        private String payToken;


        @JsonProperty("orderId")
        private String orderId;

        @JsonProperty("txnid")
        private String txnid;

        @JsonProperty("txnmode")
        private String txnmode;

        @JsonProperty("inittxnmessage")
        private String inittxnmessage;

        @JsonProperty("inittxnstatus")
        private String inittxnstatus;

        @JsonProperty("confirmtxnstatus")
        private String confirmtxnstatus;

        @JsonProperty("confirmtxnmessage")
        private String confirmtxnmessage;

        @JsonProperty("status")
        private String status;

        @JsonProperty("notifUrl")
        private String notifUrl;

        @JsonProperty("description")
        private String description;

        @JsonProperty("channelUserMsisdn")
        private String channelUserMsisdn;
    }
}
