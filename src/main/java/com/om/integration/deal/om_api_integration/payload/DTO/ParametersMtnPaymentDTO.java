package com.om.integration.deal.om_api_integration.payload.DTO;

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
public class ParametersMtnPaymentDTO {
    @JsonProperty("operation")
    private String operation;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("subscriberMsisdn")
    private String subscriberMsisdn;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("notifUrl")
    private String notifUrl;

    @JsonProperty("MessageId")
    private String messageId;
}
