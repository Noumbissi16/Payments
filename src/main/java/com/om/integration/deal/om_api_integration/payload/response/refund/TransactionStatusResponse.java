package com.om.integration.deal.om_api_integration.payload.response.refund;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class TransactionStatusResponse {
    private Result result;
    private Parameters parameters;
    @JsonProperty("CreateAt")
    private String createAt;

    @JsonProperty("MessageId")
    private String messageId;

    @JsonProperty("RefundStep")
    private String refundStep;

    @Data
    public static class Result {
        private String message;
        private Dataa data;
        private String createtime;
        private String subscriberMsisdn;
        private String amount;
        private String payToken;
        private String txnid;
        private String txnmode;
        private String txnstatus;
        private String orderId;
        private String status;
        private String channelUserMsisdn;
        private String description;
        private String fromChannelMsisdn;
        private String toChannelMsisdn;

        @Data
        public static class Dataa {
            private String createtime;
            private String subscriberMsisdn;
            private String amount;
            private String payToken;
            private String txnid;
            private String txnmode;
            private String txnstatus;
            private String orderId;
            private String status;
            private String channelUserMsisdn;
            private String description;
            private String fromChannelMsisdn;
            private String toChannelMsisdn;

        }
    }

    @Data
    public static class Parameters {
        private String amount;
        private String xauth;

        @JsonProperty("channel_user_msisdn")
        private String channelUserMsisdn;

        @JsonProperty("customer_key")
        private String customerKey;

        @JsonProperty("customer_secret")
        private String customerSecret;

        private String final_customer_name;
        private String final_customer_phone;
    }
}
