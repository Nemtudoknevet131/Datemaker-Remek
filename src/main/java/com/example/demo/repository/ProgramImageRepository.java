package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.ProgramImage;

public interface ProgramImageRepository extends JpaRepository<ProgramImage, Long> {
    List<ProgramImage> findByUserIdOrPartnerId(Long userId, Long partnerId);
}