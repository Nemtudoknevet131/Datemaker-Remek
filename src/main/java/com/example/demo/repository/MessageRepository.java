package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
            SELECT m FROM Message m
            WHERE (m.sender.id = :userId AND m.receiver.id = :partnerId)
            OR (m.sender.id = :partnerId AND m.receiver.id = :userId)
            ORDER BY m.createdAt ASC
            """)
    List<Message> findConversation(@Param("userId") Long userId,
            @Param("partnerId") Long partnerId);
}