package com.example.demo.model;

import java.time.LocalDate;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Table(name = "partner_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Request from who
    private Long fromUserId;

    // Request to who
    private Long toUserId;

    // 3 status arguments : PENDING, REJECTED, ACCEPTED
    private String status;

    // When was the request sent
    private LocalDate createdAt;
}