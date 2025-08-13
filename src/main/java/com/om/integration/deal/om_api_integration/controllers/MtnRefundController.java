package com.om.integration.deal.om_api_integration.controllers;


import com.om.integration.deal.om_api_integration.payload.request.refund.mtn.MtnCashoutRequest;
import com.om.integration.deal.om_api_integration.payload.request.refund.mtn.MtnCashoutStatusRequest;
import com.om.integration.deal.om_api_integration.payload.request.refund.mtn.MtnRefundRequestApi;
import com.om.integration.deal.om_api_integration.services.MtnRefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/refund/mtn")
public class MtnRefundController {

    @Autowired
    MtnRefundService mtnRefundService;

    @GetMapping("/token")
    public ResponseEntity<?> getAccessTokenRequest() {
        return mtnRefundService.getAccessTokenRequestMTN();
    }

    @PostMapping("/refund")
    public ResponseEntity<?> initiateCashout( @RequestBody MtnRefundRequestApi cashoutRequest) {
        return mtnRefundService.initiateCashout(cashoutRequest);
    }

    @GetMapping("/status")
    public ResponseEntity<?> getCashoutStatus(
            @RequestBody MtnCashoutStatusRequest mtnCashoutStatusRequest
    ) {
        return mtnRefundService.getCashoutStatus(
                mtnCashoutStatusRequest.getMessageId(),
                mtnCashoutStatusRequest.getAccessToken()
        );
    }




}
