package com.syed.QuizYa.repository;

import com.syed.QuizYa.model.EventGuest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EventGuestRepository extends JpaRepository<EventGuest, Long> {
    Optional<EventGuest> findBySessionToken(String sessionToken);
}
