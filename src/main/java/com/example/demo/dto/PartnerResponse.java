package com.example.demo.dto;

import java.time.LocalDate;
import com.example.demo.model.User;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerResponse {
    private boolean hasPartner;
    private User partner;
    private LocalDate relationshipStartDate;
}