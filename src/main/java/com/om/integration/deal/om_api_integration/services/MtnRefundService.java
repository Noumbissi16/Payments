package com.om.integration.deal.om_api_integration.services;

import com.om.integration.deal.om_api_integration.payload.request.refund.mtn.MtnCashoutRequest;
import com.om.integration.deal.om_api_integration.payload.request.refund.mtn.MtnRefundRequestApi;
import com.om.integration.deal.om_api_integration.payload.request.refund.om.RefundRequest;
import com.om.integration.deal.om_api_integration.payload.response.payment.mtn.MtnRefundStatusMyApiResponse;
import com.om.integration.deal.om_api_integration.payload.response.payment.mtn.MtnTransactionStatusResponse;
import com.om.integration.deal.om_api_integration.payload.response.refund.mtn.MtnCashoutResponse;
import org.springframework.http.ResponseEntity;

public interface MtnRefundService {

    ResponseEntity<MtnCashoutResponse> initiateCashout(MtnRefundRequestApi cashoutRequest);

    ResponseEntity<MtnTransactionStatusResponse> getCashoutStatus(
            String messageId,
            String accessToken
    );

    ResponseEntity<?> getAccessTokenRequestMTN();
}
