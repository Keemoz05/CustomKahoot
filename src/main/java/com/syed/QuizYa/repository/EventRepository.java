package com.syed.QuizYa.repository;

import com.syed.QuizYa.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * REPOSITORY EXPLANATION:
 * A Repository is an interface that acts as a bridge between your Java code and the Database.
 * 
 * Syntax Breakdown: extends JpaRepository<EntityClass, PrimaryKeyType>
 * - EntityClass (Event): The specific database table this repository manages.
 * - PrimaryKeyType (Long): The data type of the @Id column in the Event.java file.
 *
 * By extending JpaRepository, Spring automatically generates standard database commands 
 * in the background (like .save(), .findAll(), .deleteById()) so you don't have to write raw SQL!
 */
public interface EventRepository extends JpaRepository<Event, Long> {
    
    /**
     * JPA Magic Query: 
     * Spring reads the method name "findByJoinCode" and translates it into:
     * "SELECT * FROM events WHERE join_code = ?"
     * 
     * Using Optional<Event> is a safe way to say: "This might find an Event, or it might return nothing (null)."
     */
    Optional<Event> findByJoinCode(String joinCode);
}
