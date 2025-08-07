package com.om.integration.deal.om_api_integration.payload.request.refund.om;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusCheckBodyRequest {
    @NotBlank(message = "messageId is required to identify the transaction")
    private String messageId;
    @NotBlank(message = "Access token is required")
    private String accessToken;
}
