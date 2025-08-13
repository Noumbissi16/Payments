package com.om.integration.deal.om_api_integration.payload.request.refund.mtn;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MtnRefundRequestApi {

    @NotBlank(message = "Amount is required")
    private String amount;

    @NotBlank(message = "Customer Name is required")
    private String customerName;

    @NotBlank(message = "Customer Phone number is required")
    private String customerPhoneNumber;

    private String accessToken;

    @NotBlank(message = "Customer message is required")
    private String payerMessage;
}
