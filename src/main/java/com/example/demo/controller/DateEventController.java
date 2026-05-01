package com.example.demo.controller;

import java.util.List;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.dto.DateEventDto;
import com.example.demo.dto.DateEventRequest;
import com.example.demo.model.DateEvent;
import com.example.demo.model.User;
import com.example.demo.repository.DateEventRepository;
import com.example.demo.service.UserService;
import com.google.rpc.context.AttributeContext.Response;
import com.google.type.Date;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dates")
public class DateEventController {
    private final DateEventRepository dateEventRepository;
    private final UserService userService;

    public DateEventController(DateEventRepository dateEventRepository, UserService userService) {
        this.dateEventRepository = dateEventRepository;
        this.userService = userService;
    }

    @GetMapping("/day")
    public ResponseEntity<List<DateEventDto>> getEventsForDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, Authentication auth) {
        String email = auth.getName();
        User me = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Long> ownerIds = new ArrayList<>();
        ownerIds.add(me.getId());
        if (me.getPartner() != null) {
            ownerIds.add(me.getPartner().getId());
        }

        List<DateEvent> events = dateEventRepository.findByOwnerIdInAndDate(ownerIds, date);

        List<DateEventDto> dto = events.stream().map(e -> DateEventDto.from(e, me)).toList();

        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<DateEventDto> createEvent(@RequestBody DateEventRequest request, Authentication auth) {
        String email = auth.getName();
        User me = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DateEvent event = new DateEvent();
        event.setOwner(me);
        event.setDate(request.getDate());
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setColorHex(
                request.getColorHex() != null && !request.getColorHex().isBlank() ? request.getColorHex() : "#FFFFFF");
        event.setTime(request.getTime());

        DateEvent saved = dateEventRepository.save(event);

        return ResponseEntity.ok(DateEventDto.from(saved, me));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DateEventDto> updateEvent(@PathVariable Long id, @RequestBody DateEventRequest request,
            Authentication auth) {
        String email = auth.getName();
        User me = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DateEvent event = dateEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Long eventOwnerId = event.getOwner().getId();
        Long meId = me.getId();
        Long partnerId = me.getPartner() != null ? me.getPartner().getId() : null;

        if (!eventOwnerId.equals(meId) && (partnerId == null || !eventOwnerId.equals(partnerId))) {
            return ResponseEntity.status(403).build();
        }

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getColorHex() != null && !request.getColorHex().isBlank()) {
            event.setColorHex(request.getColorHex());
        }
        if (request.getTime() != null && !request.getTime().isBlank()) {
            event.setTime(request.getTime());
        }

        DateEvent saved = dateEventRepository.save(event);

        return ResponseEntity.ok(DateEventDto.from(saved, me));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id, Authentication auth) {
        String email = auth.getName();
        User me = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DateEvent event = dateEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Long eventOwnerId = event.getOwner().getId();
        Long meId = me.getId();

        if (!eventOwnerId.equals(meId)) {
            return ResponseEntity.status(403).build();
        }

        dateEventRepository.delete(event);
        return ResponseEntity.noContent().build();
    }
}