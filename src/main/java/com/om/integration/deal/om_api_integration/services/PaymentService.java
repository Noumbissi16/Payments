package com.om.integration.deal.om_api_integration.services;

import com.om.integration.deal.om_api_integration.payload.request.payment.mtn.MtnPaymentUserRequest;
import com.om.integration.deal.om_api_integration.payload.request.payment.om.NotifUrlRequestBody;
import com.om.integration.deal.om_api_integration.payload.request.payment.om.PaymentRequest;
import org.springframework.http.ResponseEntity;

public interface PaymentService {

    public ResponseEntity<?> getAccessTokenRequest();

    public  ResponseEntity<?> initiatePayment(String accessToken);

    public ResponseEntity<?> makePayment(PaymentRequest paymentRequest
    );

    public ResponseEntity<?> getPaymentStatus(String accessToken,String paymentToken);

    public ResponseEntity<?> ntsCashinNotifUrl(NotifUrlRequestBody notifUrlRequestBody);
}
