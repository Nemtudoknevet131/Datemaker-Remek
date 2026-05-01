package com.example.demo.dto;

import java.time.LocalDate;

import com.example.demo.model.DateEvent;
import com.example.demo.model.User;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateEventDto {
    private Long id;
    private Long ownerId;
    private String ownerName;
    private LocalDate date;
    private String title;
    private String description;
    private String colorHex;
    private String time;
    private boolean canDelete;

    public static DateEventDto from(DateEvent e, User currentUser) {
        String ownerName;

        if (e.getOwner().getId().equals(currentUser.getId())) {
            ownerName = "You";
        } else if (e.getOwner().getFirstName() != null && !e.getOwner().getFirstName().isBlank()) {
            ownerName = e.getOwner().getFirstName();
        } else {
            ownerName = "Partner";
        }

        boolean canDelete = e.getOwner().getId().equals(currentUser.getId());

        return new DateEventDto(
                e.getId(),
                e.getOwner().getId(),
                ownerName,
                e.getDate(),
                e.getTitle(),
                e.getDescription(),
                e.getColorHex(),
                e.getTime(),
                canDelete);
    }
}