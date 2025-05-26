package com.om.integration.deal.om_api_integration.payload.request.payment;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentRequest {
    @NotBlank(message = "Access token is required")
    private String accessToken;

    @NotBlank(message = "Payment token is required")
    private String paymentToken;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Amount is required")
    private String amount;

    @Size(max = 255, message = "Description too long")
    private String description;

    @NotBlank(message = "orderId is required")
    private String orderId;
}
