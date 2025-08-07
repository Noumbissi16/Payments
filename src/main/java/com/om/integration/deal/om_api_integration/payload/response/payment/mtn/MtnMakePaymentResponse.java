package com.om.integration.deal.om_api_integration.payload.response.payment.mtn;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.om.integration.deal.om_api_integration.payload.DTO.ParametersMtnPaymentDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MtnMakePaymentResponse {
    @JsonProperty("ErrorCode")
    private int errorCode;

    @JsonProperty("body")
    private String body;

    @JsonProperty("ErrorMessage")
    private String errorMessage;

    @JsonProperty("parameters")
    private ParametersMtnPaymentDTO parameters;

//    @JsonProperty("MessageId")
//    private String messageId;

}
