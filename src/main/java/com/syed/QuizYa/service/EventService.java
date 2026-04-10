package com.syed.QuizYa.service;

import com.syed.QuizYa.model.Event;
import com.syed.QuizYa.model.QuestionBank;
import com.syed.QuizYa.repository.EventRepository;
import com.syed.QuizYa.repository.QuestionBankRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
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

    private static final String ALPHANUMERIC_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final SecureRandom random = new SecureRandom();

    public EventService(EventRepository eventRepository, QuestionBankRepository questionBankRepository) {
        this.eventRepository = eventRepository;
        this.questionBankRepository = questionBankRepository;
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
}
