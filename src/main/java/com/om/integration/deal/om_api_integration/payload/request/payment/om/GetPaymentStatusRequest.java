package com.om.integration.deal.om_api_integration.payload.request.payment.om;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GetPaymentStatusRequest {
    @NotBlank(message = "Access token is required")
    String accessToken;
    @NotBlank(message = "Payment token is required")
    String paymentToken;
}
