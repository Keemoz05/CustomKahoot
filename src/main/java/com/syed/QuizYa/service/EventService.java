package com.syed.QuizYa.service;

import com.syed.QuizYa.model.Event;
import com.syed.QuizYa.model.QuestionBank;
import com.syed.QuizYa.repository.EventRepository;
import com.syed.QuizYa.repository.QuestionBankRepository;
import com.syed.QuizYa.service.QrCodeGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SERVICE LAYER EXPLANATION:
 * This service handles all logic related to Events, such as generating safe Join Codes.
 * It sits between the Controller (which asks for an event to be created) and the 
 * Repository (which actually saves the event).
 */
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final QuestionBankRepository questionBankRepository;
    private final QuestionService questionService;

    private static final String ALPHANUMERIC_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final SecureRandom random = new SecureRandom();

    public EventService(EventRepository eventRepository, QuestionBankRepository questionBankRepository, QuestionService questionService) {
        this.eventRepository = eventRepository;
        this.questionBankRepository = questionBankRepository;
        this.questionService = questionService;
    }

    public List<Event> getAllEvents() {
        // Later we will filter by Host ID when auth is added (Week 7)
        return eventRepository.findAll();
    }

    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    /**
     * @Transactional guarantees that when an event is created, a Default Bank is ALWAYS
     * created with it successfully. If either one fails, both are rolled back.
     */
    @Transactional
    public Event createEvent(String title, String description) {
        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setStatus("DRAFT");
        
        // Generate a unique 6-character join code
        String code;
        do {
            code = generateRandomCode(6);
        } while (eventRepository.findByJoinCode(code).isPresent());
        
        event.setJoinCode(code);
        event = eventRepository.save(event);

        // Create a default QuestionBank for the event
        QuestionBank bank = new QuestionBank();
        bank.setEvent(event);
        bank.setTitle(title + " - Default Bank");
        bank.setCurated(false);
        questionBankRepository.save(bank);

        return event;
    }

    private String generateRandomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC_CHARS.charAt(random.nextInt(ALPHANUMERIC_CHARS.length())));
        }
        return sb.toString();
    }

    @Transactional
    public Event updateEventTitle(Long id, String newTitle) {
        Event event = eventRepository.findById(id).orElseThrow();
        event.setTitle(newTitle);
        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        questionService.deleteEventContent(id);
        eventRepository.deleteById(id);
    }

    /**
     * Transitions an event from DRAFT to LOBBY.
     * Generates a QR code pointing to the guest join URL and saves it on the event.
     *
     * @param eventId   The event to go live
     * @param baseUrl   The server base URL (e.g. "http://localhost:8080") injected from properties
     * @throws IllegalStateException if the event is not in DRAFT status
     */
    @Transactional
    //called from host api controller
    public Event goLive(Long eventId, String baseUrl) {
        Event event = eventRepository.findById(eventId).orElseThrow();

        if (!"DRAFT".equals(event.getStatus())) {
            throw new IllegalStateException("Event must be in DRAFT status to go live");
        }

        event.setStatus("LOBBY");
        event.setStartedAt(OffsetDateTime.now());

        // Build the join URL guests will scan — e.g. http://localhost:8080/join?pin=ABC123
        String joinUrl = baseUrl + "/join?pin=" + event.getJoinCode();
        try {
            String qrDataUri = QrCodeGenerator.generateDataUri(joinUrl);
            event.setQrCodeUrl(qrDataUri);
        } catch (Exception e) {
            // QR generation failure should not block going live — log and continue
            System.err.println("QR code generation failed: " + e.getMessage());
        }

        return eventRepository.save(event);
    }

    /**
     * Finds an event by its unique join code.
     * Used by DisplayController to load the venue lobby page.
     */
    public Optional<Event> getEventByJoinCode(String joinCode) {
        return eventRepository.findByJoinCode(joinCode);
    }
}
