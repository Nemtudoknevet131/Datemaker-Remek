package com.example.demo.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "date_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who created this event
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnoreProperties({
            "partner", "password", "emailVerificationToken",
            "phoneVerificationCode", "passwordResetToken"
    })
    private User owner;

    // Day of the event
    private LocalDate date;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Boxes color in hex
    private String colorHex;

    private String time;
}