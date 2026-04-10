package com.syed.QuizYa.config;

import com.syed.QuizYa.model.QuestionBank;
import com.syed.QuizYa.model.Question;
import com.syed.QuizYa.model.QuestionOption;
import com.syed.QuizYa.repository.QuestionBankRepository;
import com.syed.QuizYa.repository.QuestionOptionRepository;
import com.syed.QuizYa.repository.QuestionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initCuratedBanks(
            QuestionBankRepository bankRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository optionRepository) {

        return args -> {
            // Check if any curated bank exists
            List<QuestionBank> curatedBanks = bankRepository.findByIsCuratedTrue();
            if (curatedBanks.isEmpty()) {
                // Create a basic Icebreaker template template
                QuestionBank icebreakerTemplate = new QuestionBank();
                icebreakerTemplate.setTitle("Icebreaker Questions Template");
                icebreakerTemplate.setDescription("A set of fun questions to get the crowd engaged");
                icebreakerTemplate.setCurated(true);
                // Note: event is null for curated banks not tied to a specific session
                bankRepository.save(icebreakerTemplate);

                // Add Question 1: Multiple Choice
                Question q1 = new Question();
                q1.setBank(icebreakerTemplate);
                q1.setQuestionType("MULTIPLE_CHOICE");
                q1.setPromptText("How are you feeling today?");
                q1.setSortOrder(1);
                q1.setPointsValue(100);
                q1.setTimeLimitSeconds(20);
                questionRepository.save(q1);

                addOption(optionRepository, q1, "Energized! \u26A1", false, 1, "#E21B3C");
                addOption(optionRepository, q1, "Need coffee \u2615", true, 2, "#1368CE");
                addOption(optionRepository, q1, "Ready to learn \uD83D\uDCDA", false, 3, "#D89E00");
                addOption(optionRepository, q1, "Still waking up \uD83D\uDE34", false, 4, "#26890C");

                // Add Question 2: Poll
                Question q2 = new Question();
                q2.setBank(icebreakerTemplate);
                q2.setQuestionType("POLL");
                q2.setPromptText("What is your preferred working style?");
                q2.setSortOrder(2);
                q2.setPointsValue(0);
                q2.setTimeLimitSeconds(30);
                questionRepository.save(q2);

                addOption(optionRepository, q2, "Remote \uD83C\uDFE0", false, 1, "#E21B3C");
                addOption(optionRepository, q2, "Office \uD83C\uDFE2", false, 2, "#1368CE");
                addOption(optionRepository, q2, "Hybrid \uD83C\uDF10", false, 3, "#D89E00");
                addOption(optionRepository, q2, "Coffee Shop \u2615", false, 4, "#26890C");

            }
        };
    }

    private void addOption(QuestionOptionRepository repo, Question q, String text, boolean isCorrect, int order, String color) {
        QuestionOption opt = new QuestionOption();
        opt.setQuestion(q);
        opt.setOptionText(text);
        opt.setCorrect(isCorrect);
        opt.setSortOrder(order);
        opt.setColorHex(color);
        repo.save(opt);
    }
}
