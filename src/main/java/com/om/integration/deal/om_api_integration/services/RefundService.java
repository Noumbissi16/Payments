package com.om.integration.deal.om_api_integration.services;

import com.om.integration.deal.om_api_integration.payload.request.refund.RefundRequest;
import org.springframework.http.ResponseEntity;

public interface RefundService {
    public ResponseEntity<?> getAccessTokenRefund();

    public ResponseEntity<?> refundPayment(RefundRequest refundRequest);

    public ResponseEntity<?> refundStatus(String accessToken, String refundTransactionId);

    public ResponseEntity<?> accountBalance(String accessToken);
}
