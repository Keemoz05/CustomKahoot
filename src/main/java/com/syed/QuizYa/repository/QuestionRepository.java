package com.syed.QuizYa.repository;

import com.syed.QuizYa.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Syntax Breakdown: extends JpaRepository<Question, Long>
 * - Question: The Model (database table) being managed.
 * - Long: The type of the Primary Key (the ID) used in the Question class.
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * Example of finding a question by its prompt text dynamically:
     * java.util.List<Question> findByPromptText(String promptText);
     * 
     * Below is your existing method. Spring parses "findByBankIdOrderBySortOrderAsc" as:
     * "SELECT * FROM questions WHERE bank_id = ? ORDER BY sort_order ASC"
     */
    java.util.List<Question> findByBankIdOrderBySortOrderAsc(Long bankId);
}
