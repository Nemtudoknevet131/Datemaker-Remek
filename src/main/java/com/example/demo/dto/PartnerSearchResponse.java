package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerSearchResponse {
    private UserDto user;
    private boolean alreadyPartnered;
    private boolean pendingRequestFromMe;
}