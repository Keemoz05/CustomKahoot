package com.syed.QuizYa.service;

import com.syed.QuizYa.model.Event;
import com.syed.QuizYa.model.EventGuest;
import com.syed.QuizYa.model.GuestAnswer;
import com.syed.QuizYa.model.QuestionOption;
import com.syed.QuizYa.repository.EventGuestRepository;
import com.syed.QuizYa.repository.EventRepository;
import com.syed.QuizYa.repository.GuestAnswerRepository;
import com.syed.QuizYa.repository.QuestionOptionRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@Service
public class GuestService {

    private final EventRepository eventRepository;
    private final EventGuestRepository eventGuestRepository;
    private final GuestAnswerRepository guestAnswerRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public GuestService(EventRepository eventRepository,
                        EventGuestRepository eventGuestRepository,
                        GuestAnswerRepository guestAnswerRepository,
                        QuestionOptionRepository questionOptionRepository,
                        SimpMessagingTemplate messagingTemplate) {
        this.eventRepository = eventRepository;
        this.eventGuestRepository = eventGuestRepository;
        this.guestAnswerRepository = guestAnswerRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public EventGuest joinEvent(String joinCode, String displayName) {
        Event event = eventRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid PIN"));

        if (!"LOBBY".equals(event.getStatus()) && !"LIVE".equals(event.getStatus())) {
            throw new IllegalArgumentException("Event is not currently open for joining");
        }

        EventGuest guest = new EventGuest();
        guest.setEvent(event);
        guest.setDisplayName(displayName);
        guest.setSessionToken(UUID.randomUUID().toString());
        guest = eventGuestRepository.save(guest);

        // Notify the lobby display
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "GUEST_JOINED");
        payload.put("guestName", displayName);
        payload.put("guestId", guest.getId());

        messagingTemplate.convertAndSend("/topic/event/" + event.getId() + "/lobby", (Object) payload);

        return guest;
    }

    public EventGuest getGuestByToken(String token) {
        return eventGuestRepository.findBySessionToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid session token"));
    }

    public java.util.List<EventGuest> getGuestsForEvent(Long eventId) {
        return eventGuestRepository.findByEventId(eventId);
    }

    @Transactional
    public boolean submitAnswer(Long guestId, Long optionId) {
        EventGuest guest = eventGuestRepository.findById(guestId)
                .orElseThrow(() -> new IllegalArgumentException("Guest not found"));
                
        QuestionOption option = questionOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("Option not found"));
                
        GuestAnswer answer = new GuestAnswer();
        answer.setEvent(guest.getEvent());
        answer.setGuest(guest);
        answer.setQuestion(option.getQuestion());
        answer.setSelectedOption(option);
        
        boolean isCorrect = Boolean.TRUE.equals(option.getCorrect());
        answer.setCorrect(isCorrect);
        
        guestAnswerRepository.save(answer);
        
        if (isCorrect) {
            guest.setCorrectCount(guest.getCorrectCount() + 1);
            eventGuestRepository.save(guest);
        }
        
        return isCorrect;
    }
    // ─── Leaderboard Helpers (Week 5) ──────────────────────────────────────
    //
    // Ranking is based ONLY on the number of correct answers (correctCount).
    // There is NO speed bonus — a guest who answers correctly in 1 second
    // earns the same credit as one who answers correctly in 30 seconds.
    // This matches the project requirement: "accuracy-based scoring logic
    // (no speed bonus — equal marks for all correct answers regardless of time)"
    // from TIMELINE.md Week 5.
    //
    // The repository method findByEventIdOrderByCorrectCountDesc returns
    // guests sorted from highest to lowest correctCount, so position in
    // that list directly equals their rank.
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Calculates the rank (1-based) of a specific guest within an event.
     * Loads all guests sorted by correctCount DESC, then walks the list
     * to find the target guest's position.  Returns 0 if the guest is
     * not found (should not happen in normal operation).
     *
     * Called by HostApiController.showLeaderboard() to build private
     * RANK_UPDATE messages sent to each guest's /queue/guest/{id} topic.
     */
    @Transactional(readOnly = true)
    public int calculateRank(Long eventId, Long guestId) {
        java.util.List<EventGuest> guests = eventGuestRepository.findByEventIdOrderByCorrectCountDesc(eventId);
        for (int i = 0; i < guests.size(); i++) {
            if (guests.get(i).getId().equals(guestId)) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * Clears all guest data for an event so it can be re-played from scratch.
     * Deletes answers first to avoid foreign key constraint violations, then
     * deletes the guests.
     */
    @Transactional
    public void clearGuestsForEvent(Long eventId) {
        java.util.List<GuestAnswer> answers = guestAnswerRepository.findByEventId(eventId);
        guestAnswerRepository.deleteAll(answers);
        
        java.util.List<EventGuest> guests = eventGuestRepository.findByEventId(eventId);
        eventGuestRepository.deleteAll(guests);
    }

    @Transactional
    public void kickGuest(Long guestId) {
        guestAnswerRepository.deleteAll(guestAnswerRepository.findByGuestId(guestId));
        eventGuestRepository.deleteById(guestId);
    }

    /**
     * Returns the top N guests for an event, sorted by correctCount DESC.
     * Used by HostApiController.showLeaderboard() to build the top-5 list
     * that gets broadcast to the venue display and all guest screens.
     *
     * The "score" shown on the leaderboard IS the correctCount — there is
     * no separate scoring formula.
     */
    @Transactional(readOnly = true)
    public java.util.List<EventGuest> getTopGuests(Long eventId, int limit) {
        java.util.List<EventGuest> guests = eventGuestRepository.findByEventIdOrderByCorrectCountDesc(eventId);
        return guests.stream().limit(limit).collect(java.util.stream.Collectors.toList());
    }
}

