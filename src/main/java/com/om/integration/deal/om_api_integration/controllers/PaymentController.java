package com.om.integration.deal.om_api_integration.controllers;


import com.om.integration.deal.om_api_integration.payload.request.payment.GetPaymentStatusRequest;
import com.om.integration.deal.om_api_integration.payload.request.payment.NotifUrlRequestBody;
import com.om.integration.deal.om_api_integration.payload.request.payment.OmMakePaymentRequest;
import com.om.integration.deal.om_api_integration.payload.request.payment.PaymentRequest;
import com.om.integration.deal.om_api_integration.services.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @Autowired
    PaymentService paymentService;

    @PostMapping("/token")
    public ResponseEntity<?> getAccessTokenRequest() {
        return paymentService.getAccessTokenRequest();
    }
    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(
            @RequestParam(name = "accessToken", required = true) String accessToken
    ) {
        return paymentService.initiatePayment(accessToken);
    }
    @PostMapping(value = "/make")
    public ResponseEntity<?> makePayment(
            @Valid @RequestBody PaymentRequest paymentRequest
    ) {
        return paymentService.makePayment(paymentRequest);
    }
    @GetMapping("/status")
    public ResponseEntity<?> getPaymentStatus(
            @RequestParam("accessToken") String accessToken,
            @RequestParam("payToken") String payToken)
    {
        return paymentService.getPaymentStatus(accessToken, payToken);
    }

    @PostMapping("/nts/cashin/notifUrl")
    public ResponseEntity<?> ntsCashinNotifUrl( @RequestBody NotifUrlRequestBody notifUrlRequestBody) {
        return paymentService.ntsCashinNotifUrl(notifUrlRequestBody);
    }
}
