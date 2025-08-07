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
public class MTNMakePaymentRequestApiMut {
    @JsonProperty("notifUrl")
    private String notifUrl;

    @JsonProperty("subscriberMsisdn")
    private String subscriberMsisdn;

    @JsonProperty("description")
    private String description;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("customersecret")
    private String customerSecret;

    @JsonProperty("customerkey")
    private String customerKey;

    @JsonProperty("PaiementMethod")
    private String paiementMethod;
}
