package com.om.integration.deal.om_api_integration.controllers;


import com.om.integration.deal.om_api_integration.payload.request.refund.RefundRequest;
import com.om.integration.deal.om_api_integration.payload.request.refund.StatusCheckBodyRequest;
import com.om.integration.deal.om_api_integration.payload.request.refund.StatusCheckRequest;
import com.om.integration.deal.om_api_integration.services.RefundService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/refund")
public class RefundController {
    @Autowired
    RefundService refundService;

    @GetMapping("/token")
    public ResponseEntity<?> getAccessTokenRequest() {
        return refundService.getAccessTokenRefund();
    }

    @PostMapping("/refund")
    public ResponseEntity<?> refundPayment(
           @Valid @RequestBody RefundRequest refundRequest
    ){
        return refundService.refundPayment(refundRequest);
    }

    @GetMapping("/status")
    public ResponseEntity<?> getTransactionStatus(
            @Valid @RequestBody StatusCheckBodyRequest statusCheckBodyRequest
    ) {
        return refundService.refundStatus(
                statusCheckBodyRequest.getAccessToken(),
                statusCheckBodyRequest.getMessageId()
        );
    }

}
