package com.syed.QuizYa.service;

import com.syed.QuizYa.model.Question;
import com.syed.QuizYa.model.QuestionBank;
import com.syed.QuizYa.model.QuestionOption;
import com.syed.QuizYa.repository.QuestionBankRepository;
import com.syed.QuizYa.repository.QuestionOptionRepository;
import com.syed.QuizYa.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SERVICE LAYER EXPLANATION:
 * The Service layer contains the "Business Logic". It does calculation, checks rules, 
 * and tells the Repositories what to save or delete. 
 * 
 * @Service tells Spring Boot to create exactly one instance of this class to use everywhere.
 */
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final QuestionBankRepository questionBankRepository;
    private final com.syed.QuizYa.repository.GuestAnswerRepository guestAnswerRepository;
    private final com.syed.QuizYa.repository.EventRepository eventRepository;

    public QuestionService(QuestionRepository questionRepository, 
                           QuestionOptionRepository questionOptionRepository,
                           QuestionBankRepository questionBankRepository,
                           com.syed.QuizYa.repository.GuestAnswerRepository guestAnswerRepository,
                           com.syed.QuizYa.repository.EventRepository eventRepository) {
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.questionBankRepository = questionBankRepository;
        this.guestAnswerRepository = guestAnswerRepository;
        this.eventRepository = eventRepository;
    }

    public List<Question> getQuestionsForBank(Long bankId) {
        return questionRepository.findByBankIdOrderBySortOrderAsc(bankId);
    }
    
    public List<QuestionOption> getOptionsForQuestion(Long questionId) {
        return questionOptionRepository.findByQuestionIdOrderBySortOrderAsc(questionId);
    }

    public List<QuestionBank> getEventBanks(Long eventId) {
        return questionBankRepository.findByEventId(eventId);
    }

    public List<QuestionBank> getCuratedBanks() {
        return questionBankRepository.findByIsCuratedTrue();
    }

    /**
     * @Transactional ensures "All or Nothing".
     * If the database crashes after saving the question but before creating the options,
     * Spring Boot will rollback the question creation safely so we don't have broken data.
     */
    @Transactional
    public Question addQuestion(Long bankId, String type) {
        QuestionBank bank = questionBankRepository.findById(bankId)
            .orElseThrow(() -> new RuntimeException("Bank not found"));

        List<Question> existing = questionRepository.findByBankIdOrderBySortOrderAsc(bankId);
        int nextSortOrder = existing.size() + 1;

        Question q = new Question();
        q.setBank(bank);
        q.setQuestionType(type);
        q.setPromptText("New Question");
        q.setSortOrder(nextSortOrder);
        q.setTimeLimitSeconds(30);
        q = questionRepository.save(q);

        // Auto-add 4 options for MCQs/Polls
        if ("MULTIPLE_CHOICE".equals(type) || "POLL".equals(type)) {
            String[] colors = {"#E21B3C", "#1368CE", "#D89E00", "#26890C"};
            for (int i = 0; i < 4; i++) {
                QuestionOption opt = new QuestionOption();
                opt.setQuestion(q);
                opt.setOptionText("Option " + (i + 1));
                opt.setSortOrder(i + 1);
                opt.setCorrect(false);
                opt.setColorHex(colors[i]);
                questionOptionRepository.save(opt);
            }
        }
        
        return q;
    }

    /**
     * Updates an existing question. If the user changes it to a Word Cloud or Slide,
     * this method has business logic to actively delete the old orphaned options (A,B,C,D).
     */
    @Transactional
    public Question updateQuestion(Long questionId, String type, String prompt, Integer timeLimitSeconds) {
        Question q = questionRepository.findById(questionId).orElseThrow();
        
        boolean typeChanged = !q.getQuestionType().equals(type);
        q.setQuestionType(type);
        q.setPromptText(prompt);
        if (timeLimitSeconds != null) {
            q.setTimeLimitSeconds(timeLimitSeconds);
        }
        q = questionRepository.save(q);

        if (typeChanged && ("WORD_CLOUD".equals(type) || "SLIDE".equals(type))) {
            // Delete options since they are not used for these types
            List<QuestionOption> options = questionOptionRepository.findByQuestionIdOrderBySortOrderAsc(questionId);
            questionOptionRepository.deleteAll(options);
        } else if (typeChanged && ("MULTIPLE_CHOICE".equals(type) || "POLL".equals(type))) {
            List<QuestionOption> options = questionOptionRepository.findByQuestionIdOrderBySortOrderAsc(questionId);
            if (options.isEmpty()) {
                String[] colors = {"#E21B3C", "#1368CE", "#D89E00", "#26890C"};
                for (int i = 0; i < 4; i++) {
                    QuestionOption opt = new QuestionOption();
                    opt.setQuestion(q);
                    opt.setOptionText("Option " + (i + 1));
                    opt.setSortOrder(i + 1);
                    opt.setCorrect(false);
                    opt.setColorHex(colors[i]);
                    questionOptionRepository.save(opt);
                }
            } else if ("POLL".equals(type)) {
                // If switching to poll, clear all "correct" flags
                for (QuestionOption opt : options) {
                    opt.setCorrect(false);
                    questionOptionRepository.save(opt);
                }
            }
        }

        return q;
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        List<QuestionOption> options = questionOptionRepository.findByQuestionIdOrderBySortOrderAsc(questionId);
        questionOptionRepository.deleteAll(options);
        questionRepository.deleteById(questionId);
    }

    @Transactional
    public QuestionOption addOption(Long questionId) {
        Question q = questionRepository.findById(questionId).orElseThrow();
        List<QuestionOption> options = questionOptionRepository.findByQuestionIdOrderBySortOrderAsc(questionId);
        
        QuestionOption opt = new QuestionOption();
        opt.setQuestion(q);
        opt.setOptionText("New Option");
        opt.setSortOrder(options.size() + 1);
        opt.setCorrect(false);
        // Basic colors loop logic or default generic color since it's flexible lengths
        String[] colors = {"#E21B3C", "#1368CE", "#D89E00", "#26890C", "#8A2BE2", "#FF7F50", "#00CED1", "#FFD700"};
        opt.setColorHex(colors[options.size() % colors.length]);
        
        return questionOptionRepository.save(opt);
    }

    @Transactional
    public QuestionOption updateOption(Long optionId, String text, Boolean isCorrect) {
        QuestionOption opt = questionOptionRepository.findById(optionId).orElseThrow();
        opt.setOptionText(text);
        if ("POLL".equals(opt.getQuestion().getQuestionType())) {
            opt.setCorrect(false); // Force false for polls
        } else {
            opt.setCorrect(isCorrect != null ? isCorrect : false);
        }
        return questionOptionRepository.save(opt);
    }

    @Transactional
    public void deleteOption(Long optionId) {
        questionOptionRepository.deleteById(optionId);
    }

    @Transactional
    public void importBank(Long targetBankId, Long sourceBankId) {
        QuestionBank targetBank = questionBankRepository.findById(targetBankId).orElseThrow();
        List<Question> sourceQuestions = questionRepository.findByBankIdOrderBySortOrderAsc(sourceBankId);
        
        List<Question> targetExisting = questionRepository.findByBankIdOrderBySortOrderAsc(targetBankId);
        int nextSort = targetExisting.size() + 1;

        for (Question sourceQ : sourceQuestions) {
            Question newQ = new Question();
            newQ.setBank(targetBank);
            newQ.setQuestionType(sourceQ.getQuestionType());
            newQ.setPromptText(sourceQ.getPromptText());
            newQ.setSortOrder(nextSort++);
            newQ.setTimeLimitSeconds(sourceQ.getTimeLimitSeconds());
            newQ.setExplanationText(sourceQ.getExplanationText());
            newQ = questionRepository.save(newQ);

            // Clone options
            List<QuestionOption> sourceOptions = questionOptionRepository.findByQuestionIdOrderBySortOrderAsc(sourceQ.getId());
            for (QuestionOption sourceOpt : sourceOptions) {
                QuestionOption newOpt = new QuestionOption();
                newOpt.setQuestion(newQ);
                newOpt.setOptionText(sourceOpt.getOptionText());
                newOpt.setCorrect(sourceOpt.getCorrect());
                newOpt.setSortOrder(sourceOpt.getSortOrder());
                newOpt.setColorHex(sourceOpt.getColorHex());
                questionOptionRepository.save(newOpt);
            }
        }
    }

    @Transactional
    public void deleteEventContent(Long eventId) {
        List<QuestionBank> banks = questionBankRepository.findByEventId(eventId);
        for (QuestionBank bank : banks) {
            List<Question> questions = questionRepository.findByBankIdOrderBySortOrderAsc(bank.getId());
            for (Question q : questions) {
                List<QuestionOption> options = questionOptionRepository.findByQuestionIdOrderBySortOrderAsc(q.getId());
                questionOptionRepository.deleteAll(options);
            }
            questionRepository.deleteAll(questions);
        }
        questionBankRepository.deleteAll(banks);
    }

    public java.util.Map<String, Object> getVoteDistribution(Long eventId) {
        com.syed.QuizYa.model.Event event = eventRepository.findById(eventId).orElseThrow();
        int currentIdx = event.getCurrentQuestionIndex();
        
        if (currentIdx <= 0) return new java.util.HashMap<>();
        
        List<QuestionBank> banks = questionBankRepository.findByEventId(eventId);
        if (banks.isEmpty()) return new java.util.HashMap<>();
        
        List<Question> questions = getQuestionsForBank(banks.get(0).getId());
        if (currentIdx > questions.size()) return new java.util.HashMap<>();
        
        Question current = questions.get(currentIdx - 1);
        List<QuestionOption> options = getOptionsForQuestion(current.getId());
        
        // Count answers
        List<com.syed.QuizYa.model.GuestAnswer> eventAnswers = guestAnswerRepository.findByEventId(eventId);
        java.util.Map<Long, Long> optionCounts = new java.util.HashMap<>();
        long totalCount = 0;
        
        for (com.syed.QuizYa.model.GuestAnswer ans : eventAnswers) {
            // Check if answer belongs to an option of this question
            boolean matches = options.stream().anyMatch(o -> o.getId().equals(ans.getSelectedOption().getId()));
            if (matches) {
                Long optId = ans.getSelectedOption().getId();
                optionCounts.put(optId, optionCounts.getOrDefault(optId, 0L) + 1);
                totalCount++;
            }
        }
        
        final long total = totalCount;
        List<java.util.Map<String, Object>> optionsList = new java.util.ArrayList<>();
        for (QuestionOption opt : options) {
            long count = optionCounts.getOrDefault(opt.getId(), 0L);
            double percentage = total > 0 ? (double) count / total * 100.0 : 0.0;
            
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", opt.getId());
            map.put("text", opt.getOptionText());
            map.put("color", opt.getColorHex() != null ? opt.getColorHex() : "#ccc");
            map.put("count", count);
            map.put("percentage", Math.round(percentage));
            optionsList.add(map);
        }
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("options", optionsList);
        result.put("totalVotes", total);
        return result;
    }
}
