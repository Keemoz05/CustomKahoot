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

    public GuestApiController(GuestService guestService, 
                              com.syed.QuizYa.service.EventService eventService,
                              com.syed.QuizYa.repository.EventGuestRepository eventGuestRepository,
                              com.syed.QuizYa.service.QuestionService questionService) {
        this.guestService = guestService;
        this.eventService = eventService;
        this.eventGuestRepository = eventGuestRepository;
        this.questionService = questionService;
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
}
