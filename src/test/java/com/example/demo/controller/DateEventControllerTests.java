package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.example.demo.dto.DateEventDto;
import com.example.demo.dto.DateEventRequest;
import com.example.demo.model.DateEvent;
import com.example.demo.model.User;
import com.example.demo.repository.DateEventRepository;
import com.example.demo.service.UserService;

@ExtendWith(MockitoExtension.class)
class DateEventControllerTests {

    @Mock
    private DateEventRepository dateEventRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private DateEventController dateEventController;

    @Test
    void getEventsForDay_returnsOnlyRequestedDayForUserAndPartner() {
        LocalDate day = LocalDate.of(2026, 4, 13);

        User partner = new User();
        partner.setId(2L);
        partner.setFirstName("Eve");

        User me = new User();
        me.setId(1L);
        me.setEmail("me@example.com");
        me.setPartner(partner);

        DateEvent mine = new DateEvent();
        mine.setId(100L);
        mine.setOwner(me);
        mine.setDate(day);
        mine.setTitle("My event");

        DateEvent partnerEvent = new DateEvent();
        partnerEvent.setId(101L);
        partnerEvent.setOwner(partner);
        partnerEvent.setDate(day);
        partnerEvent.setTitle("Partner event");

        Authentication auth = org.mockito.Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("me@example.com");
        when(userService.findByEmail("me@example.com")).thenReturn(Optional.of(me));
        when(dateEventRepository.findByOwnerIdInAndDate(eq(List.of(1L, 2L)), eq(day)))
                .thenReturn(List.of(mine, partnerEvent));

        ResponseEntity<List<DateEventDto>> response = dateEventController.getEventsForDay(day, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getDate()).isEqualTo(day);
        verify(dateEventRepository).findByOwnerIdInAndDate(List.of(1L, 2L), day);
    }

    @Test
    void createEvent_savesToDatabaseWithDefaultColorWhenMissing() {
        //1
        User me = new User();
        me.setId(1L);
        me.setEmail("me@example.com");

        //2
        DateEventRequest req = new DateEventRequest(
                LocalDate.of(2026, 4, 13),
                "Dinner",
                "At home",
                null,
                "19:00");
        //3
        DateEvent saved = new DateEvent();
        saved.setId(200L);
        saved.setOwner(me);
        saved.setDate(req.getDate());
        saved.setTitle(req.getTitle());
        saved.setDescription(req.getDescription());
        saved.setColorHex("#FFFFFF");
        saved.setTime(req.getTime());

        //4
        Authentication auth = org.mockito.Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("me@example.com");
        when(userService.findByEmail("me@example.com")).thenReturn(Optional.of(me));
        when(dateEventRepository.save(org.mockito.ArgumentMatchers.any(DateEvent.class))).thenReturn(saved);

        ResponseEntity<DateEventDto> response = dateEventController.createEvent(req, auth);
        //5
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(200L);
        assertThat(response.getBody().getColorHex()).isEqualTo("#FFFFFF");
        //6
        ArgumentCaptor<DateEvent> captor = ArgumentCaptor.forClass(DateEvent.class);
        verify(dateEventRepository).save(captor.capture());
        assertThat(captor.getValue().getDate()).isEqualTo(LocalDate.of(2026, 4, 13));
        assertThat(captor.getValue().getColorHex()).isEqualTo("#FFFFFF");
    }

    @Test
    void deleteEvent_deletesFromDatabaseWhenCurrentUserOwnsEvent() {
        User me = new User();
        me.setId(1L);
        me.setEmail("me@example.com");

        DateEvent event = new DateEvent();
        event.setId(300L);
        event.setOwner(me);

        Authentication auth = org.mockito.Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("me@example.com");
        when(userService.findByEmail("me@example.com")).thenReturn(Optional.of(me));
        when(dateEventRepository.findById(300L)).thenReturn(Optional.of(event));

        ResponseEntity<?> response = dateEventController.deleteEvent(300L, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(dateEventRepository).delete(event);
    }

    @Test
    void deleteEvent_returnsForbiddenWhenCurrentUserIsNotOwner() {
        User me = new User();
        me.setId(1L);
        me.setEmail("me@example.com");

        User owner = new User();
        owner.setId(2L);

        DateEvent event = new DateEvent();
        event.setId(300L);
        event.setOwner(owner);

        Authentication auth = org.mockito.Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn("me@example.com");
        when(userService.findByEmail("me@example.com")).thenReturn(Optional.of(me));
        when(dateEventRepository.findById(300L)).thenReturn(Optional.of(event));

        ResponseEntity<?> response = dateEventController.deleteEvent(300L, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(dateEventRepository, never()).delete(event);
    }
}
