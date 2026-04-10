package com.syed.QuizYa.controller;

import com.syed.QuizYa.model.Question;
import com.syed.QuizYa.model.QuestionBank;
import com.syed.QuizYa.model.QuestionOption;
import com.syed.QuizYa.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/host/events/{eventId}")
public class HostApiController {

    private final QuestionService questionService;

    public HostApiController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/banks")
    public ResponseEntity<?> getEventBanks(@PathVariable Long eventId) {
        List<QuestionBank> banks = questionService.getEventBanks(eventId);
        if (banks.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("banks", banks.stream().map(b -> Map.of(
            "id", b.getId(),
            "title", b.getTitle()
        )).collect(Collectors.toList())));
    }

    @GetMapping("/banks/{bankId}/questions")
    public ResponseEntity<?> getQuestions(@PathVariable Long bankId) {
        List<Question> questions = questionService.getQuestionsForBank(bankId);
        List<Map<String, Object>> qList = questions.stream().map(q -> {
            List<QuestionOption> options = questionService.getOptionsForQuestion(q.getId());
            return Map.of(
                "id", q.getId(),
                "type", q.getQuestionType(),
                "prompt", q.getPromptText(),
                "sortOrder", q.getSortOrder(),
                "points", q.getPointsValue(),
                "timeLimit", q.getTimeLimitSeconds() != null ? q.getTimeLimitSeconds() : 20,
                "options", options.stream().map(o -> Map.of(
                    "id", o.getId(),
                    "text", o.getOptionText(),
                    "isCorrect", o.getCorrect(),
                    "sortOrder", o.getSortOrder(),
                    "color", o.getColorHex() != null ? o.getColorHex() : "#ccc"
                )).collect(Collectors.toList())
            );
        }).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("questions", qList));
    }

    @PostMapping("/banks/{bankId}/questions")
    public ResponseEntity<?> addQuestion(@PathVariable Long bankId, @RequestBody Map<String, String> payload) {
        String type = payload.getOrDefault("type", "MULTIPLE_CHOICE");
        Question q = questionService.addQuestion(bankId, type);
        return ResponseEntity.ok(Map.of("success", true, "id", q.getId()));
    }

    @PutMapping("/questions/{qId}")
    public ResponseEntity<?> updateQuestion(@PathVariable Long qId, @RequestBody Map<String, Object> payload) {
        String type = (String) payload.get("type");
        String prompt = (String) payload.get("prompt");
        Integer timeLimit = payload.containsKey("timeLimit") ? Integer.valueOf(payload.get("timeLimit").toString()) : 20;
        Integer points = payload.containsKey("points") ? Integer.valueOf(payload.get("points").toString()) : 100;
        
        questionService.updateQuestion(qId, type, prompt, timeLimit, points);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/questions/{qId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long qId) {
        questionService.deleteQuestion(qId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/questions/{qId}/options")
    public ResponseEntity<?> addOption(@PathVariable Long qId) {
        QuestionOption opt = questionService.addOption(qId);
        return ResponseEntity.ok(Map.of("success", true, "id", opt.getId(), "color", opt.getColorHex()));
    }

    @PutMapping("/options/{optId}")
    public ResponseEntity<?> updateOption(@PathVariable Long optId, @RequestBody Map<String, Object> payload) {
        String text = (String) payload.get("text");
        Boolean isCorrect = (Boolean) payload.get("isCorrect");
        questionService.updateOption(optId, text, isCorrect);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/options/{optId}")
    public ResponseEntity<?> deleteOption(@PathVariable Long optId) {
        questionService.deleteOption(optId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/curated-banks")
    public ResponseEntity<?> getCuratedBanks() {
        List<QuestionBank> banks = questionService.getCuratedBanks();
        return ResponseEntity.ok(Map.of("banks", banks.stream().map(b -> Map.of(
            "id", b.getId(),
            "title", b.getTitle(),
            "description", b.getDescription() != null ? b.getDescription() : ""
        )).collect(Collectors.toList())));
    }

    @PostMapping("/banks/{targetBankId}/import/{sourceBankId}")
    public ResponseEntity<?> importBank(@PathVariable Long targetBankId, @PathVariable Long sourceBankId) {
        questionService.importBank(targetBankId, sourceBankId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
