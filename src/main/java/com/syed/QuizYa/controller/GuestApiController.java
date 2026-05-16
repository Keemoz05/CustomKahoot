package com.syed.QuizYa.controller;

import com.syed.QuizYa.service.GuestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/guest")
public class GuestApiController {

    private final GuestService guestService;

    // Week 5: EventService and EventGuestRepository were added so that when a
    // guest submits an answer, we can broadcast an ANSWER_SUBMITTED event to
    // the /topic/event/{id}/lobby channel.  The host's live controls page
    // (host-live.js) listens for this event and increments the "Answers
    // Submitted" counter in real time, giving the host visibility into how
    // many guests have responded before they lock answers.
    private final com.syed.QuizYa.service.EventService eventService;
    private final com.syed.QuizYa.repository.EventGuestRepository eventGuestRepository;
    private final com.syed.QuizYa.service.QuestionService questionService;
    private final com.syed.QuizYa.repository.GuestAnswerRepository guestAnswerRepository;

    public GuestApiController(GuestService guestService, 
                              com.syed.QuizYa.service.EventService eventService,
                              com.syed.QuizYa.repository.EventGuestRepository eventGuestRepository,
                              com.syed.QuizYa.service.QuestionService questionService,
                              com.syed.QuizYa.repository.GuestAnswerRepository guestAnswerRepository) {
        this.guestService = guestService;
        this.eventService = eventService;
        this.eventGuestRepository = eventGuestRepository;
        this.questionService = questionService;
        this.guestAnswerRepository = guestAnswerRepository;
    }

    @PostMapping("/{guestId}/answer")
    public ResponseEntity<?> submitAnswer(
            @PathVariable Long guestId,
            @RequestBody Map<String, Long> payload) {
            
        Long optionId = payload.get("optionId");
        if (optionId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Option ID is required"));
        }
        
        try {
            boolean isCorrect = guestService.submitAnswer(guestId, optionId);
            
            com.syed.QuizYa.model.EventGuest guest = eventGuestRepository.findById(guestId).orElse(null);
            if (guest != null) {
                Long eventId = guest.getEvent().getId();
                eventService.broadcastToDisplay(eventId, java.util.Map.of("type", "ANSWER_SUBMITTED"));
                
                // Broadcast vote distribution
                Map<String, Object> distPayload = questionService.getVoteDistribution(eventId);
                distPayload.put("type", "VOTE_DISTRIBUTION");
                eventService.broadcastToDisplay(eventId, distPayload);
            }
            
            return ResponseEntity.ok(Map.of("success", true, "correct", isCorrect));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{guestId}/state")
    public ResponseEntity<?> getGuestState(@PathVariable Long guestId) {
        com.syed.QuizYa.model.EventGuest guest = eventGuestRepository.findById(guestId).orElse(null);
        if (guest == null) return ResponseEntity.notFound().build();
        
        com.syed.QuizYa.model.Event event = guest.getEvent();
        if (event == null) return ResponseEntity.notFound().build();

        int currentIndex = event.getCurrentQuestionIndex();
        
        // Find current question ID
        Long currentQuestionId = null;
        if (currentIndex > 0) {
            java.util.List<com.syed.QuizYa.model.QuestionBank> banks = questionService.getEventBanks(event.getId());
            if (!banks.isEmpty()) {
                java.util.List<com.syed.QuizYa.model.Question> questions = questionService.getQuestionsForBank(banks.get(0).getId());
                if (currentIndex <= questions.size()) {
                    currentQuestionId = questions.get(currentIndex - 1).getId();
                }
            }
        }

        Long selectedOptionId = null;
        Boolean isCorrect = null;
        java.util.List<Long> correctOptionIds = new java.util.ArrayList<>();
        if (currentQuestionId != null) {
            com.syed.QuizYa.model.GuestAnswer answer = guestAnswerRepository.findByGuestIdAndQuestionId(guestId, currentQuestionId);
            if (answer != null) {
                selectedOptionId = answer.getSelectedOption().getId();
                isCorrect = answer.getCorrect();
            }
            
            if (Boolean.TRUE.equals(event.getAnswersRevealed())) {
                java.util.List<com.syed.QuizYa.model.QuestionOption> options = questionService.getOptionsForQuestion(currentQuestionId);
                for (com.syed.QuizYa.model.QuestionOption opt : options) {
                    if (Boolean.TRUE.equals(opt.getCorrect())) {
                        correctOptionIds.add(opt.getId());
                    }
                }
            }
        }

        return ResponseEntity.ok(Map.of(
            "success", true,
            "status", event.getStatus(),
            "answersRevealed", event.getAnswersRevealed(),
            "selectedOptionId", selectedOptionId != null ? selectedOptionId : -1,
            "isCorrect", isCorrect != null ? isCorrect : false,
            "correctOptionIds", correctOptionIds
        ));
    }
}
