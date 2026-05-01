package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.PartnerRequestDto;
import com.example.demo.dto.PartnerResponse;
import com.example.demo.model.PartnerRequest;
import com.example.demo.service.PartnerRequestService;
import com.example.demo.service.UserService;
import com.google.rpc.context.AttributeContext.Response;

@RestController
@RequestMapping("/api/partner")
public class PartnerController {

    private static final Logger log = LoggerFactory.getLogger(PartnerController.class);

    private final UserService userService;
    private final PartnerRequestService partnerRequestService;

    public PartnerController(UserService userService, PartnerRequestService partnerRequestService) {
        this.userService = userService;
        this.partnerRequestService = partnerRequestService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addPartner(@RequestParam Long userId, @RequestParam String partnerEmail) {
        log.info("[/api/partner/add] called, userId={}, partnerEmail={}", userId, partnerEmail);
        boolean success = userService.addPartner(userId, partnerEmail);

        if (success) {
            log.info("[/api/partner/add] OK");
            return ResponseEntity.ok("Partner added successfully");
        } else {
            log.warn("[/api/partner/add] FAILED (not found or already connected)");
            return ResponseEntity.badRequest().body("Partner not found or already connected");
        }
    }

    @PostMapping("/request")
    public ResponseEntity<?> sendPartnerRequest(@RequestParam Long fromUserId, @RequestParam String partnerEmail) {
        log.info("[/api/partner/request] called, fromUserId={}, partnerEmail={}", fromUserId, partnerEmail);

        try {
            partnerRequestService.createRequest(fromUserId, partnerEmail);
            log.info("[/api/partner/request] createRequest finished");
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.warn("[/api/partner/request] failed: {}", e.getMessage());

            if (e.getMessage().contains("pending")) {
                return ResponseEntity.status(409).body(e.getMessage());
            }

            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/requests")
    public ResponseEntity<List<PartnerRequestDto>> getRequests(@RequestParam Long userId) {
        log.info("[/api/partner/requests] called, userId={}", userId);
        List<PartnerRequestDto> pending = partnerRequestService.getPendingRequestForUser(userId);
        log.info("[/api/partner/requests] returning {} items", pending.size());
        return ResponseEntity.ok(pending);
    }

    @PostMapping("/accept/{requestId}")
    public ResponseEntity<?> acceptRequest(@PathVariable Long requestId) {
        log.info("[/api/partner/accept/{}] called", requestId);
        partnerRequestService.acceptRequest(requestId);
        log.info("[/api/partner/accept/{}] done", requestId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject/{requestId}")
    public ResponseEntity<?> rejectRequest(@PathVariable Long requestId) {
        log.info("[/api/partner/reject/{}] called", requestId);
        partnerRequestService.rejectRequest(requestId);
        log.info("[/api/partner/reject/{}] done", requestId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<PartnerResponse> getPartner(@PathVariable Long userId) {
        log.info("[/api/partner/{}] called", userId);
        var userOpt = userService.findById(userId);

        if (userOpt.isEmpty()) {
            log.warn("[/api/partner/{}] user not found", userId);
            return ResponseEntity.notFound().build();
        }

        var user = userOpt.get();
        var partner = user.getPartner();

        PartnerResponse resp = new PartnerResponse();
        resp.setHasPartner(partner != null);
        resp.setPartner(partner);

        resp.setRelationshipStartDate(user.getRelationshipStartDate());

        log.info("[/api/partner/{}] hasPartner={}", userId, partner != null);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/isPartnered/{userId}")
    public ResponseEntity<?> isPartnered(@PathVariable("userId") Long id) {
        log.info("[/api/partner/isPartnered/{}] called", id);
        var userOpt = userService.findById(id);

        if (userOpt.isEmpty()) {
            log.warn("[/api/partner/isPartnered/{}] user not found", id);
            return ResponseEntity.badRequest().body("User not found");
        }

        var user = userOpt.get();
        boolean partnered = user.getPartner() != null;
        log.info("[/api/partner/isPartnered/{}] partnered={}", id, partnered);
        return ResponseEntity.ok(partnered);
    }

    @DeleteMapping("/remove/{userId}")
    public ResponseEntity<?> removePartner(@PathVariable Long userId) {
        log.info("[/api/partner/remove/{}] called", userId);
        boolean success = userService.removePartner(userId);
        if (success) {
            log.info("[/api/partner/remove/{}] removed", userId);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("[/api/partner/remove/{}] user not found", userId);
            return ResponseEntity.badRequest().body("User not found");
        }
    }

    @PatchMapping("/relationship-date/{userId}")
    public ResponseEntity<?> setRelationshipDate(@PathVariable Long userId, @RequestParam String date) {
        log.info("[/api/partner/relationship-date/{}] called, date={}", userId, date);

        userService.setRelationshipDate(userId, LocalDate.parse(date));
        return ResponseEntity.ok().build();
    }
}