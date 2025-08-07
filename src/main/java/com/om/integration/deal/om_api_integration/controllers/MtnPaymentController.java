package com.om.integration.deal.om_api_integration.controllers;


import com.om.integration.deal.om_api_integration.payload.request.payment.mtn.MtnPaymentUserRequest;
import com.om.integration.deal.om_api_integration.payload.request.payment.mtn.MtnStatusRequest;
import com.om.integration.deal.om_api_integration.payload.response.payment.mtn.MtnMakePaymentResponse;
import com.om.integration.deal.om_api_integration.payload.response.payment.mtn.MtnTokenResponse;
import com.om.integration.deal.om_api_integration.services.MtnPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payments/mtn")
public class MtnPaymentController {

    @Autowired
    MtnPaymentService mtnPaymentService;

    /**
     * Get access token for MTN API
     * @return ResponseEntity with token response
     */
    @GetMapping("/token")
    public ResponseEntity<MtnTokenResponse> getAccessToken() {
        return mtnPaymentService.getAccessTokenRequestMTN();
    }

    /**
     * Initiate a collection (CashIn) payment
     * @param request Collection request data
     * @return ResponseEntity with collection response
     */
    @PostMapping("/make")
    public ResponseEntity<MtnMakePaymentResponse> initiateCollection(
            @RequestBody MtnPaymentUserRequest request
    ) {
       return mtnPaymentService.makePaymentMTN(request);
    }

    @GetMapping("/status")
    public ResponseEntity<?> getTransactionStatus(
            @RequestBody MtnStatusRequest request
    ) {
        return mtnPaymentService.getPaymentStatusMTN(request.getMessageId(), request.getAuthToken());
    }

}
