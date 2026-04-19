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

    public GuestApiController(GuestService guestService, 
                              com.syed.QuizYa.service.EventService eventService,
                              com.syed.QuizYa.repository.EventGuestRepository eventGuestRepository) {
        this.guestService = guestService;
        this.eventService = eventService;
        this.eventGuestRepository = eventGuestRepository;
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
            
            // Week 5: After persisting the answer, broadcast an ANSWER_SUBMITTED
            // event to the lobby/display channel.  This lets the host's live
            // controls page update the "Answers Submitted" counter in real time.
            // We look up the guest to find their event ID for the broadcast.
            com.syed.QuizYa.model.EventGuest guest = eventGuestRepository.findById(guestId).orElse(null);
            if (guest != null) {
                eventService.broadcastToDisplay(guest.getEvent().getId(), java.util.Map.of("type", "ANSWER_SUBMITTED"));
            }
            
            return ResponseEntity.ok(Map.of("success", true, "correct", isCorrect));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
