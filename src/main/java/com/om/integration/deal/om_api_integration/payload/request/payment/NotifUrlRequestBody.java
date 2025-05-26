package com.om.integration.deal.om_api_integration.payload.request.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotifUrlRequestBody {
    String payToken;
    String status;
    String message;
}
