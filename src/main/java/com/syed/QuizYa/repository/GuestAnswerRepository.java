package com.syed.QuizYa.repository;

import com.syed.QuizYa.model.GuestAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuestAnswerRepository extends JpaRepository<GuestAnswer, Long> {
    List<GuestAnswer> findByEventId(Long eventId);
}
