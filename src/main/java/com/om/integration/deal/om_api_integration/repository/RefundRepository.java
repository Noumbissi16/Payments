package com.om.integration.deal.om_api_integration.repository;

import com.om.integration.deal.om_api_integration.model.RefundResponse;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RefundRepository extends MongoRepository<RefundResponse, String> {
}
