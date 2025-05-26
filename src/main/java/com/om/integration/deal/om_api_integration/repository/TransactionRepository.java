package com.om.integration.deal.om_api_integration.repository;


import com.om.integration.deal.om_api_integration.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    Optional<Transaction> findByPayToken(String payToken);
    Optional<Transaction> findByMessageId(String payToken);
}
