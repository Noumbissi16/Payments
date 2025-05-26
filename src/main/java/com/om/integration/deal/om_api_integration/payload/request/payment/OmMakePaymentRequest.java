package com.om.integration.deal.om_api_integration.payload.request.payment;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OmMakePaymentRequest {
    @JsonProperty("notifUrl")
    private String notifUrl;

    @JsonProperty("channelUserMsisdn")
    private String channelUserMsisdn;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("subscriberMsisdn")
    private String subscriberMsisdn;

    @JsonProperty("pin")
    private String pin;

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("description")
    private String description;

    @JsonProperty("payToken")
    private String payToken;
}
