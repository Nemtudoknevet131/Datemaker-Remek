package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.MessageDto;
import com.example.demo.dto.SendMessageRequest;
import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.repository.MessageRepository;

@ExtendWith(MockitoExtension.class)
class MessageServiceTests {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserService userService;

    @Mock
    private FcmService fcmService;

    @InjectMocks
    private MessageService messageService;

    @Test
    void sendMessage_savesMessageAndReturnsDto() {
        User sender = new User();
        sender.setId(1L);

        User receiver = new User();
        receiver.setId(2L);

        SendMessageRequest request = new SendMessageRequest(2L, "Hello there");

        when(userService.findById(1L)).thenReturn(Optional.of(sender));
        when(userService.findById(2L)).thenReturn(Optional.of(receiver));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message msg = invocation.getArgument(0);
            msg.setId(99L);
            return msg;
        });

        MessageDto dto = messageService.sendMessage(1L, request);

        assertThat(dto.getId()).isEqualTo(99L);
        assertThat(dto.getSenderId()).isEqualTo(1L);
        assertThat(dto.getReceiverId()).isEqualTo(2L);
        assertThat(dto.getContent()).isEqualTo("Hello there");
        assertThat(dto.isReadFlag()).isFalse();

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
        assertThat(captor.getValue().isReadFlag()).isFalse();

        verify(fcmService).sendChatMessage(receiver, dto);
    }

    @Test
    void sendMessage_throwsWhenReceiverMissing() {
        User sender = new User();
        sender.setId(1L);

        when(userService.findById(1L)).thenReturn(Optional.of(sender));
        when(userService.findById(2L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> messageService.sendMessage(1L, new SendMessageRequest(2L, "Hi")));

        assertThat(ex.getMessage()).isEqualTo("Receiver not found");
    }

    @Test
    void getConversation_returnsMappedDtosInRepositoryOrder() {
        User sender = new User();
        sender.setId(1L);

        User receiver = new User();
        receiver.setId(2L);

        Message m1 = new Message();
        m1.setId(11L);
        m1.setSender(sender);
        m1.setReceiver(receiver);
        m1.setContent("First");
        m1.setCreatedAt(LocalDateTime.now().minusMinutes(5));

        Message m2 = new Message();
        m2.setId(12L);
        m2.setSender(receiver);
        m2.setReceiver(sender);
        m2.setContent("Second");
        m2.setCreatedAt(LocalDateTime.now());

        when(messageRepository.findConversation(1L, 2L)).thenReturn(List.of(m1, m2));

        List<MessageDto> conversation = messageService.getConversation(1L, 2L);

        assertThat(conversation).hasSize(2);
        assertThat(conversation.get(0).getContent()).isEqualTo("First");
        assertThat(conversation.get(1).getContent()).isEqualTo("Second");
    }
}
