package com.om.integration.deal.om_api_integration.services;

import com.om.integration.deal.om_api_integration.payload.request.payment.mtn.MtnPaymentUserRequest;
import com.om.integration.deal.om_api_integration.payload.response.payment.mtn.MtnMakePaymentResponse;
import com.om.integration.deal.om_api_integration.payload.response.payment.mtn.MtnTokenResponse;
import org.springframework.http.ResponseEntity;

public interface MtnPaymentService {
    public ResponseEntity<MtnTokenResponse> getAccessTokenRequestMTN();

    public  ResponseEntity<MtnMakePaymentResponse> makePaymentMTN(MtnPaymentUserRequest mtnPaymentUserRequest);

    public ResponseEntity<?> getPaymentStatusMTN(String messageId, String authToken);

}
