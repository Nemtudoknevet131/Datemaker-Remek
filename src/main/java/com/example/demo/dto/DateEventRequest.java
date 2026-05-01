package com.example.demo.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateEventRequest {
    private LocalDate date;
    private String title;
    private String description;
    private String colorHex;
    private String time;
}