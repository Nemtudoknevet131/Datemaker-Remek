package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.model.PartnerRequest;
import com.example.demo.model.User;
import com.example.demo.push.FireBaseManager;
import com.example.demo.repository.PartnerRequestRepository;
import com.example.demo.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PartnerRequestServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PartnerRequestRepository partnerRequestRepository;

    @Mock
    private FireBaseManager fireBaseManager;

    @Mock
    private UserService userService;

    @InjectMocks
    private PartnerRequestService partnerRequestService;

    @Test
    void createRequest_savesPendingRequestAndSendsNotification() {
        User from = new User();
        from.setId(1L);
        from.setFirstName("John");
        from.setLastName("Doe");

        User to = new User();
        to.setId(2L);
        to.setEmail("target@example.com");
        to.setFcmToken("token-123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(from));
        when(userService.findByEmailOrUsername("target@example.com")).thenReturn(Optional.of(to));
        when(partnerRequestRepository.existsByFromUserIdAndToUserIdAndStatus(1L, 2L, "PENDING")).thenReturn(false);

        partnerRequestService.createRequest(1L, "target@example.com");

        ArgumentCaptor<PartnerRequest> captor = ArgumentCaptor.forClass(PartnerRequest.class);
        verify(partnerRequestRepository).save(captor.capture());

        PartnerRequest saved = captor.getValue();
        assertThat(saved.getFromUserId()).isEqualTo(1L);
        assertThat(saved.getToUserId()).isEqualTo(2L);
        assertThat(saved.getStatus()).isEqualTo("PENDING");

        verify(fireBaseManager).sendPartnerRequestNotif("token-123", "John Doe");
    }

    @Test
    void acceptRequest_marksAcceptedAndConnectsUsersAsCouple() {
        PartnerRequest req = new PartnerRequest();
        req.setId(77L);
        req.setFromUserId(1L);
        req.setToUserId(2L);
        req.setStatus("PENDING");

        User target = new User();
        target.setId(2L);
        target.setEmail("target@example.com");

        when(partnerRequestRepository.findById(77L)).thenReturn(Optional.of(req));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        partnerRequestService.acceptRequest(77L);

        assertThat(req.getStatus()).isEqualTo("ACCEPTED");
        verify(partnerRequestRepository).save(req);
        verify(userService).addPartner(1L, "target@example.com");
    }

    @Test
    void createRequest_throwsWhenPendingAlreadyExists() {
        User from = new User();
        from.setId(1L);

        User to = new User();
        to.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(from));
        when(userService.findByEmailOrUsername("target@example.com")).thenReturn(Optional.of(to));
        when(partnerRequestRepository.existsByFromUserIdAndToUserIdAndStatus(1L, 2L, "PENDING")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> partnerRequestService.createRequest(1L, "target@example.com"));

        assertThat(ex.getMessage()).contains("pending request");
    }
}
