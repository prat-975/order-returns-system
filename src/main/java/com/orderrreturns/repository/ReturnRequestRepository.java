package com.orderrreturns.repository;

import com.orderrreturns.entity.ReturnRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnRequestRepository extends MongoRepository<ReturnRequest, String> {

    List<ReturnRequest> findBySubmittedByOrderByCreatedAtDesc(String submittedBy);

    List<ReturnRequest> findAllByOrderByCreatedAtDesc();
}
