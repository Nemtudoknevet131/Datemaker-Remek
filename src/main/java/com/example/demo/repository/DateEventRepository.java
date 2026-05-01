package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.DateEvent;

public interface DateEventRepository extends JpaRepository<DateEvent, Long> {

    // All events by date
    List<DateEvent> findByOwnerIdInAndDate(Iterable<Long> ownerIds, LocalDate date);
}