package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.PartnerRequest;

import java.util.List;

public interface PartnerRequestRepository extends JpaRepository<PartnerRequest, Long>{

    // All the requests the user got and is pending
    List<PartnerRequest> findByToUserIdAndStatus(Long toUserId, String status);

    boolean existsByFromUserIdAndToUserIdAndStatus(Long fromUserId, Long toUserId, String status);

    boolean existsByToUserIdAndStatus(Long toUserId, String status);
}