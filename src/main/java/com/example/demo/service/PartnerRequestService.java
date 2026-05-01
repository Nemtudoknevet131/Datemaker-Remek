package com.example.demo.service;

import com.example.demo.dto.PartnerRequestDto;
import com.example.demo.model.PartnerRequest;
import com.example.demo.model.User;
import com.example.demo.push.FireBaseManager;
import com.example.demo.repository.PartnerRequestRepository;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PartnerRequestService {
    private static final Logger log = LoggerFactory.getLogger(PartnerRequestService.class);

    private final UserRepository userRepository;
    private final PartnerRequestRepository partnerRequestRepository;
    private final FireBaseManager fireBaseManager;
    private final UserService userService;

    public PartnerRequestService(UserRepository userRepository,
            PartnerRequestRepository partnerRequestRepository,
            FireBaseManager fireBaseManager,
            UserService userService) {
        this.userRepository = userRepository;
        this.partnerRequestRepository = partnerRequestRepository;
        this.fireBaseManager = fireBaseManager;
        this.userService = userService;
    }

    public void createRequest(long fromUserId, String partnerEmailOrUsername) {
        log.info("[PartnerRequestService] createRequest fromUserId={}, partnerEmail={}", fromUserId,
                partnerEmailOrUsername);

        User from = userRepository.findById(fromUserId)
                .orElseThrow(() -> {
                    log.error("[PartnerRequestService] sender not found, id={}", fromUserId);
                    return new RuntimeException("Sender not found");
                });

        User to = userService.findByEmailOrUsername(partnerEmailOrUsername)
                .orElseThrow(() -> {
                    log.error("[PartnerRequestService] target user not found, query={}", partnerEmailOrUsername);
                    return new RuntimeException("Target user not found");
                });

        if (from.getId().equals(to.getId())) {
            log.warn("[PartnerRequestService] user {} tried to send request to themselves", from.getId());
            throw new RuntimeException("You cannot send a request to yourself");
        }

        if (from.getPartner() != null || to.getPartner() != null) {
            log.warn("[PartnerRequestService] someone already has a partner (fromHas={}, toHas={})",
                    from.getPartner() != null, to.getPartner() != null);
            throw new RuntimeException("One of the users already have a partner");
        }

        if (partnerRequestRepository.existsByFromUserIdAndToUserIdAndStatus(from.getId(), to.getId(), "PENDING")) {
            log.warn("[PartnerRequestService] a pending request already exists from {} to {}", from.getId(),
                    to.getId());
            throw new RuntimeException("You already sent a pending request to this user");
        }

        PartnerRequest pr = new PartnerRequest();
        pr.setFromUserId(from.getId());
        pr.setToUserId(to.getId());
        pr.setStatus("PENDING");
        pr.setCreatedAt(LocalDate.now());

        partnerRequestRepository.save(pr);
        log.info("[PartnerRequestService] request saved with id={}", pr.getId());

        if (to.getFcmToken() != null && !to.getFcmToken().isEmpty()) {
            String fullName = (from.getFirstName() != null ? from.getFirstName() : "")
                    + " "
                    + (from.getLastName() != null ? from.getLastName() : "");
            log.info("[PartnerRequestService] sending FCM to userId={} token={}", to.getId(), to.getFcmToken());
            fireBaseManager.sendPartnerRequestNotif(to.getFcmToken(), fullName.trim());
        } else {
            log.info("[PartnerRequestService] target user has NO fcmToken, skipping push");
        }
    }

    public List<PartnerRequestDto> getPendingRequestForUser(Long userId) {
        log.info("[PartnerRequestService] getPendingRequestForUser userId={}", userId);
        return partnerRequestRepository.findByToUserIdAndStatus(userId, "PENDING")
                .stream()
                .map(this::toDto)
                .toList();
    }

    private PartnerRequestDto toDto(PartnerRequest pr) {
        PartnerRequestDto dto = new PartnerRequestDto();
        dto.setId(pr.getId());
        dto.setFromUserId(pr.getFromUserId());
        dto.setToUserId(pr.getToUserId());
        dto.setStatus(pr.getStatus());
        dto.setCreatedAt(pr.getCreatedAt());

        User from = userRepository.findById(pr.getFromUserId())
                .orElse(null);

        String name = "Unknown user";
        if (from != null) {
            String first = from.getFirstName();
            String last = from.getLastName();
            String username = from.getUsername();

            if (first != null && !first.isBlank() && last != null && !last.isBlank()) {
                name = first + " " + last;
            } else if (first != null && !first.isBlank()) {
                name = first;
            } else if (username != null && !username.isBlank()) {
                name = username;
            } else {
                name = from.getEmail();
            }
        }

        dto.setFromUserName(name);

        return dto;
    }

    public void acceptRequest(Long requestId) {
        log.info("[PartnerRequestService] acceptRequest id={}", requestId);
        PartnerRequest pr = partnerRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        pr.setStatus("ACCEPTED");
        partnerRequestRepository.save(pr);
        log.info("[PartnerRequestService] request {} marked ACCEPTED", requestId);

        userService.addPartner(
                pr.getFromUserId(),
                userRepository.findById(pr.getToUserId()).orElseThrow().getEmail());
    }

    public void rejectRequest(Long requestId) {
        log.info("[PartnerRequestService] rejectRequest id={}", requestId);
        PartnerRequest pr = partnerRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        pr.setStatus("REJECTED");
        partnerRequestRepository.save(pr);
        log.info("[PartnerRequestService] request {} marked REJECTED", requestId);
    }
}
