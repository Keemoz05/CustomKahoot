package com.syed.QuizYa.repository;

import com.syed.QuizYa.model.QuestionBank;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Syntax Breakdown: extends JpaRepository<QuestionBank, Long>
 * Connects the 'question_banks' table to our Java code.
 */
public interface QuestionBankRepository extends JpaRepository<QuestionBank, Long> {
    
    /**
     * Translates to: "SELECT * FROM question_banks WHERE event_id = ?" 
     */
    java.util.List<QuestionBank> findByEventId(Long eventId);
    
    /**
     * Translates to: "SELECT * FROM question_banks WHERE is_curated = true" 
     */
    java.util.List<QuestionBank> findByIsCuratedTrue();
}
