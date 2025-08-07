package com.om.integration.deal.om_api_integration.payload.request.payment.mtn;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MTNMakePaymentRequestBody {
    @JsonProperty("API_MUT")
    private MTNMakePaymentRequestApiMut API_MUT;
}
