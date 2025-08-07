package com.om.integration.deal.om_api_integration.payload.response.refund.mtn;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class MtnCashoutResponse {
    @JsonProperty("MessageId")
    private String messageId;
    private Map<String, String> queueId;
    private String errorMessage;

}
