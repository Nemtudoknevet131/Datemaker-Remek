package com.example.demo.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerRequestDto {
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private String status;
    private LocalDate createdAt;

    private String fromUserName;
}