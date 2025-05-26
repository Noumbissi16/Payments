package com.om.integration.deal.om_api_integration.repository;

import com.om.integration.deal.om_api_integration.model.RefundResponse;
import com.om.integration.deal.om_api_integration.payload.response.refund.RefundTokenResponse;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RefundRepository extends MongoRepository<RefundResponse, String> {
}
