package com.football.booking_system.repository;

import com.football.booking_system.entity.UnansweredMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnansweredMessageRepository extends JpaRepository<UnansweredMessage, Long> {
}