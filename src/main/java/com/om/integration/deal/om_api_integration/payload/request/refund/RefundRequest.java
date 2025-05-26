package com.om.integration.deal.om_api_integration.payload.request.refund;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefundRequest {
    @NotBlank(message = "Access token is required")
    private String accessToken;
    @NotBlank(message = "Amount is required")
    private String amount;
    @NotBlank(message = "Customer Phone number is required")
    private String customerPhoneNumber;
    @NotBlank(message = "Customer Name is required")
    private String customerName;
    private boolean feesIncluded;
    private boolean isDeposit;
}
