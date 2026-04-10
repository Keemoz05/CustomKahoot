package com.syed.QuizYa.repository;

import com.syed.QuizYa.model.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Syntax Breakdown: extends JpaRepository<QuestionOption, Long>
 * Defines a database manager for the 'question_options' table.
 */
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {
    
    /**
     * Example query. Spring translates this to:
     * "SELECT * FROM question_options WHERE question_id = ? ORDER BY sort_order ASC"
     */
    java.util.List<QuestionOption> findByQuestionIdOrderBySortOrderAsc(Long questionId);
}
