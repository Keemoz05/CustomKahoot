package com.syed.QuizYa.controller;

import com.syed.QuizYa.model.MediaAsset;
import com.syed.QuizYa.model.Question;
import com.syed.QuizYa.model.QuestionBank;
import com.syed.QuizYa.model.QuestionOption;
import com.syed.QuizYa.repository.QuestionRepository;
import com.syed.QuizYa.service.EventService;
import com.syed.QuizYa.service.GuestService;
import com.syed.QuizYa.service.MediaService;
import com.syed.QuizYa.service.QuestionService;
import com.syed.QuizYa.service.UrlDiscoveryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/host/events/{eventId}")
public class HostApiController {

    private final QuestionService questionService;
    private final EventService eventService;
    private final MediaService mediaService;
    private final QuestionRepository questionRepository;

    // Week 5: GuestService added so the live control endpoints can look up
    // guest data (top performers, individual ranks) when building leaderboard
    // payloads to broadcast over WebSocket.
    private final GuestService guestService;
    private final UrlDiscoveryService urlDiscoveryService;

    // Reads server.port from application.properties to build the base URL for the QR code
    @Value("${server.port:8080}")
    private int serverPort;

    public HostApiController(
        QuestionService questionService,
        EventService eventService,
        MediaService mediaService,
        QuestionRepository questionRepository,
        GuestService guestService,
        UrlDiscoveryService urlDiscoveryService) {
        this.questionService = questionService;
        this.eventService = eventService;
        this.mediaService = mediaService;
        this.questionRepository = questionRepository;
        this.guestService = guestService;
        this.urlDiscoveryService = urlDiscoveryService;
    }

    // ─── Question Banks ──────────────────────────────────────────────────────

    @GetMapping("/banks")
    public ResponseEntity<?> getEventBanks(@PathVariable Long eventId) {
        List<QuestionBank> banks = questionService.getEventBanks(eventId);
        if (banks.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("banks", banks.stream().map(b -> Map.of(
            "id", b.getId(),
            "title", b.getTitle()
        )).collect(Collectors.toList())));
    }

    /**
     * GET /banks/{bankId}/questions
     *
     * Returns the full list of questions for a bank, including media URLs so the
     * frontend can render existing upload previews when switching between questions.
     */
    @GetMapping("/banks/{bankId}/questions")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getQuestions(@PathVariable Long bankId) {
        List<Question> questions = questionService.getQuestionsForBank(bankId);
        List<Map<String, Object>> qList = questions.stream().map(q -> {
            List<QuestionOption> options = questionService.getOptionsForQuestion(q.getId());

            // Build the media info — null-safe, returns empty strings if no media attached
            MediaAsset media = q.getMediaAsset();
            String mediaUrl = (media != null && media.getOriginalUrl() != null) ? media.getOriginalUrl() : "";
            String transformedUrl = (media != null && media.getTransformedUrl() != null) ? media.getTransformedUrl() : "";
            Long mediaAssetId = (media != null) ? media.getId() : null;

            Map<String, Object> qMap = new LinkedHashMap<>();
            qMap.put("id", q.getId());
            qMap.put("type", q.getQuestionType());
            qMap.put("prompt", q.getPromptText());
            qMap.put("sortOrder", q.getSortOrder());
            qMap.put("timeLimitSeconds", q.getTimeLimitSeconds());
            qMap.put("mediaUrl", mediaUrl);
            qMap.put("transformedUrl", transformedUrl);
            qMap.put("mediaAssetId", mediaAssetId);
            qMap.put("options", options.stream().map(o -> Map.of(
                "id", o.getId(),
                "text", o.getOptionText(),
                "isCorrect", o.getCorrect(),
                "sortOrder", o.getSortOrder(),
                "color", o.getColorHex() != null ? o.getColorHex() : "#ccc"
            )).collect(Collectors.toList()));

            return qMap;
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
        Integer timeLimitSeconds = null;
        if (payload.containsKey("timeLimitSeconds") && payload.get("timeLimitSeconds") != null) {
            timeLimitSeconds = Integer.parseInt(payload.get("timeLimitSeconds").toString());
        }
        questionService.updateQuestion(qId, type, prompt, timeLimitSeconds);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/questions/{qId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long qId) {
        questionService.deleteQuestion(qId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    //Express JS Syntax Comments
    //router.post('/questions/:qId/options', async (req, res) => {
    @PostMapping("/questions/{qId}/options")
    
    // Equivalent to: const qId = req.params.qId;
    public ResponseEntity<?> addOption(@PathVariable Long qId) {
        // Equivalent to: const opt = await questionService.addOption(qId);
        QuestionOption opt = questionService.addOption(qId);
        // Equivalent to: return res.json({ success: true, id: opt.id, color: opt.colorHex });
        return ResponseEntity.ok(Map.of("success", true, "id", opt.getId(), "color", opt.getColorHex()));
    }
    // Equivalent to: router.put('/options/:optId', async (req, res) => {
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

    // ─── Question Library ────────────────────────────────────────────────────

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

    // ─── Event Settings ──────────────────────────────────────────────────────

    @GetMapping("/state")
    public ResponseEntity<?> getEventState(@PathVariable Long eventId) {
        return eventService.getEventById(eventId)
                .map(event -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "currentQuestionIndex", event.getCurrentQuestionIndex(),
                        "status", event.getStatus(),
                        "showLeaderboard", event.getShowLeaderboard(),
                        "showFeedback", event.getShowFeedback(),
                        "answersRevealed", event.getAnswersRevealed()
                )))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("")
    public ResponseEntity<?> renameEvent(@PathVariable Long eventId, @RequestBody Map<String, String> payload) {
        String newTitle = payload.get("title");
        if (newTitle != null && !newTitle.trim().isEmpty()) {
            eventService.updateEventTitle(eventId, newTitle);
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteEvent(@PathVariable Long eventId) {
        guestService.clearGuestsForEvent(eventId);
        eventService.deleteEvent(eventId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ─── Audience Management (Phase 5) ───────────────────────────────────────

    @GetMapping("/guests")
    public ResponseEntity<?> getGuests(@PathVariable Long eventId) {
        List<com.syed.QuizYa.model.EventGuest> guests = guestService.getGuestsForEvent(eventId);
        List<Map<String, Object>> list = guests.stream().map(g -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", g.getId());
            m.put("displayName", g.getDisplayName());
            m.put("correctCount", g.getCorrectCount());
            m.put("joinedAt", g.getJoinedAt().toString());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("guests", list));
    }

    @PostMapping("/kick/{guestId}")
    public ResponseEntity<?> kickGuest(@PathVariable Long eventId, @PathVariable Long guestId) {
        guestService.kickGuest(guestId);
        
        // Notify the kicked guest
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "KICKED");
        eventService.broadcastToGuest(guestId, payload);
        
        // Notify display to remove from roster
        Map<String, Object> displayPayload = new HashMap<>();
        displayPayload.put("type", "GUEST_KICKED");
        displayPayload.put("guestId", guestId);
        eventService.broadcastToDisplay(eventId, displayPayload);
        
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/cleanup-previews")
    public ResponseEntity<?> cleanupPreviews(@PathVariable Long eventId) {
        guestService.cleanupPreviewGuests(eventId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ─── Media Upload (Week 3) ───────────────────────────────────────────────

    /**
     * POST /questions/{qId}/media
     *
     * Accepts a multipart/form-data file upload, pushes it to Cloudinary,
     * creates a MediaAsset record, and links it to the question.
     *
     * The frontend sends: <input type="file"> via FormData
     */
    @PostMapping("/questions/{qId}/media")
    @Transactional
    public ResponseEntity<?> uploadMedia(
            @PathVariable Long qId,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }

        try {
            // 1. Upload to Cloudinary and create a MediaAsset record
            MediaAsset asset = mediaService.upload(file);

            // 2. Load the question and attach the new media asset
            Question question = questionRepository.findById(qId)
                    .orElseThrow(() -> new IllegalArgumentException("Question not found: " + qId));
            question.setMediaAsset(asset);
            questionRepository.save(question);

            // 3. Return the URLs so the frontend can immediately show the preview
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mediaAssetId", asset.getId());
            response.put("mediaUrl", asset.getOriginalUrl());
            response.put("transformedUrl", asset.getTransformedUrl());
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    /**
     * DELETE /questions/{qId}/media
     *
     * Unlinks the media asset from the question (sets media_asset_id = NULL).
     * Does NOT delete the asset from Cloudinary — that is an admin operation.
     */
    @DeleteMapping("/questions/{qId}/media")
    @Transactional
    public ResponseEntity<?> removeMedia(@PathVariable Long qId) {
        Question question = questionRepository.findById(qId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + qId));
        question.setMediaAsset(null);
        questionRepository.save(question);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ─── Lobby Launch (Week 3) ───────────────────────────────────────────────

    @GetMapping("/test-ngrok")
    public ResponseEntity<?> testNgrok(HttpServletRequest request) {
        String url = urlDiscoveryService.getBaseUrl(request.getServerPort());
        return ResponseEntity.ok(Map.of("url", url));
    }

    /**
     * POST /go-live
     *
     * Transitions the event from DRAFT -> LOBBY, generates a QR code, and returns
     * the data needed to show the host the join info overlay.
     *
     * Uses HttpServletRequest to derive the base URL dynamically (works on any port/host).
     */
    @PostMapping("/go-live")
    public ResponseEntity<?> goLive(@PathVariable Long eventId, HttpServletRequest request) {
        try {
            // Automatically determine the base URL using Ngrok if available, or fallback to Local IP
            String baseUrl = urlDiscoveryService.getBaseUrl(request.getServerPort());

            com.syed.QuizYa.model.Event event = eventService.goLive(eventId, baseUrl);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("joinCode", event.getJoinCode());
            response.put("qrCodeUrl", event.getQrCodeUrl());
            response.put("displayUrl", "/display/" + event.getJoinCode());

            // Week 5: Return a URL to the new Host Live Controls page so the
            // setup overlay can show a "Live Controls" link alongside the
            // existing "Venue Display" link.
            response.put("hostLiveUrl", "/host/events/" + eventId + "/live");
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            // Event is already live (LOBBY or LIVE)
            // It's possible the host started Ngrok AFTER the event went live.
            // Let's dynamically fetch the baseUrl and update the QR code so it always reflects the current network state.
            String baseUrl = urlDiscoveryService.getBaseUrl(request.getServerPort());
            com.syed.QuizYa.model.Event event = eventService.getEventById(eventId).orElseThrow();
            
            try {
                String joinUrl = baseUrl + "/join?pin=" + event.getJoinCode();
                String newQrDataUri = com.syed.QuizYa.service.QrCodeGenerator.generateDataUri(joinUrl);
                event.setQrCodeUrl(newQrDataUri);
                // Also update the database so the presenter view gets the updated QR
                eventService.saveEvent(event); 
            } catch (Exception ex) {
                System.err.println("QR code regeneration failed: " + ex.getMessage());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("joinCode", event.getJoinCode());
            response.put("qrCodeUrl", event.getQrCodeUrl());
            response.put("displayUrl", "/display/" + event.getJoinCode());
            response.put("hostLiveUrl", "/host/events/" + eventId + "/live");
            return ResponseEntity.ok(response);
        }
    }


    private ResponseEntity<?> nextQuestion(Long eventId) {
        com.syed.QuizYa.model.Event event = eventService.getEventById(eventId).orElseThrow();

        // Increment the 1-based question pointer stored on the Event entity.
        // currentQuestionIndex starts at 0 (no question shown yet).
        int newIndex = event.getCurrentQuestionIndex() + 1;
        event.setCurrentQuestionIndex(newIndex);
        event.setAnswersRevealed(false);
        event.setShowLeaderboard(false);
        event.setShowFeedback(false);
        eventService.saveEvent(event);

        // Grab the first (default) bank for this event — every event always
        // has exactly one bank created in EventService.createEvent().
        List<QuestionBank> banks = questionService.getEventBanks(eventId);
        QuestionBank defaultBank = banks.get(0);
        List<Question> questions = questionService.getQuestionsForBank(defaultBank.getId());

        // Boundary check: if we've gone past the last question, reject.
        if (event.getCurrentQuestionIndex() > questions.size()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No more questions"));
        }

        // Fetch the actual Question and its QuestionOptions.
        // Index is 1-based on the event but 0-based in the list, hence the -1.
        Question currentQuestion = questions.get(event.getCurrentQuestionIndex() - 1);
        List<QuestionOption> options = questionService.getOptionsForQuestion(currentQuestion.getId());

        // Build the WebSocket payload.  Using HashMap instead of Map.of()
        // because Map.of() produces an immutable map with strict generic
        // inference that causes compile errors when mixing Integer/String values.
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "SHOW_QUESTION");
        payload.put("prompt", currentQuestion.getPromptText());
        payload.put("options", options.stream().map(o -> {
            Map<String, Object> optMap = new HashMap<>();
            optMap.put("id", o.getId());        // needed by play.html to submit the answer
            optMap.put("text", o.getOptionText());
            optMap.put("color", o.getColorHex() != null ? o.getColorHex() : "#ccc");
            return optMap;
        }).collect(Collectors.toList()));

    // Broadcast to both channels so guests see the question on their
        // phones AND the venue projector shows it on the big screen.
        eventService.broadcastToGuests(eventId, payload);
        eventService.broadcastToDisplay(eventId, payload);
        return ResponseEntity.ok(Map.of("success", true, "state", "QUESTION"));
    }

    @PostMapping("/skip")
    public ResponseEntity<?> skipQuestion(@PathVariable Long eventId) {
        com.syed.QuizYa.model.Event event = eventService.getEventById(eventId).orElseThrow();
        
        // Skip = increment index + broadcast skipped, then call next
        event.setCurrentQuestionIndex(event.getCurrentQuestionIndex() + 1);
        
        Map<String, Object> skipPayload = new HashMap<>();
        skipPayload.put("type", "QUESTION_SKIPPED");
        eventService.broadcastToGuests(eventId, skipPayload);
        eventService.broadcastToDisplay(eventId, skipPayload);
        
        // Then immediately show next question
        return nextQuestion(eventId);
    }

    /**
     * POST /next-to
     * Jumps to a specific question without incrementing. Used by the Safe Tap feature.
     */
    @PostMapping("/next-to")
    public ResponseEntity<?> jumpToQuestion(@PathVariable Long eventId, @RequestBody Map<String, Integer> body) {
        int targetIndex = body.getOrDefault("questionIndex", 0); // Default to 0 (Lobby)
        com.syed.QuizYa.model.Event event = eventService.getEventById(eventId).orElseThrow();
        
        event.setCurrentQuestionIndex(targetIndex);
        event.setAnswersRevealed(false);
        event.setShowLeaderboard(false);
        event.setShowFeedback(false);
        eventService.saveEvent(event);
        
        if (targetIndex == 0) {
            // Show Lobby
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "SHOW_LOBBY");
            eventService.broadcastToGuests(eventId, payload);
            eventService.broadcastToDisplay(eventId, payload);
            return ResponseEntity.ok(Map.of("success", true, "state", "LOBBY"));
        }
        
        List<QuestionBank> banks = questionService.getEventBanks(eventId);
        QuestionBank defaultBank = banks.get(0);
        List<Question> questions = questionService.getQuestionsForBank(defaultBank.getId());
        
        if (targetIndex > questions.size() || targetIndex < 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid question index"));
        }
        
        Question currentQuestion = questions.get(targetIndex - 1);
        List<QuestionOption> options = questionService.getOptionsForQuestion(currentQuestion.getId());
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "SHOW_QUESTION");
        payload.put("prompt", currentQuestion.getPromptText());
        payload.put("options", options.stream().map(o -> {
            Map<String, Object> optMap = new HashMap<>();
            optMap.put("id", o.getId());
            optMap.put("text", o.getOptionText());
            optMap.put("color", o.getColorHex() != null ? o.getColorHex() : "#ccc");
            return optMap;
        }).collect(Collectors.toList()));
        
        eventService.broadcastToGuests(eventId, payload);
        eventService.broadcastToDisplay(eventId, payload);
        
        return ResponseEntity.ok(Map.of("success", true, "state", "QUESTION"));
    }

    @PostMapping("/toggle-leaderboard")
    public ResponseEntity<?> toggleLeaderboard(@PathVariable Long eventId) {
        com.syed.QuizYa.model.Event event = eventService.getEventById(eventId).orElseThrow();
        boolean newState = !event.getShowLeaderboard();
        event.setShowLeaderboard(newState);
        eventService.saveEvent(event);

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", newState ? "SHOW_LEADERBOARD" : "HIDE_LEADERBOARD");

        if (newState) {
            // Fetch the top 5 guests
            List<com.syed.QuizYa.model.EventGuest> topGuests = guestService.getTopGuests(eventId, 5);
            List<Map<String, Object>> topList = topGuests.stream().map(g -> {
                Map<String, Object> map = new HashMap<>();
                map.put("name", g.getDisplayName());
                map.put("score", g.getCorrectCount());
                return map;
            }).collect(Collectors.toList());

            payload.put("topGuests", topList);

            // Send private RANK_UPDATE
            List<com.syed.QuizYa.model.EventGuest> guests = guestService.getGuestsForEvent(eventId);
            for (com.syed.QuizYa.model.EventGuest guest : guests) {
                int rank = guestService.calculateRank(eventId, guest.getId());
                Map<String, Object> rankPayload = new HashMap<>();
                rankPayload.put("type", "RANK_UPDATE");
                rankPayload.put("rank", rank);
                rankPayload.put("correctCount", guest.getCorrectCount());
                eventService.broadcastToGuest(guest.getId(), rankPayload);
            }
        }

        eventService.broadcastToGuests(eventId, payload);
        eventService.broadcastToDisplay(eventId, payload);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        responseBody.put("showLeaderboard", newState);
        if (newState && payload.containsKey("topGuests")) {
            responseBody.put("topGuests", payload.get("topGuests"));
        }

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/toggle-feedback")
    public ResponseEntity<?> toggleFeedback(@PathVariable Long eventId) {
        com.syed.QuizYa.model.Event event = eventService.getEventById(eventId).orElseThrow();
        boolean newState = !event.getShowFeedback();
        event.setShowFeedback(newState);
        eventService.saveEvent(event);

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", newState ? "SHOW_FEEDBACK" : "HIDE_FEEDBACK");
        
        // Let clients know to display or hide the feedback view. The data itself 
        // will be pulled or pushed separately, but we could also broadcast it here if needed.
        eventService.broadcastToGuests(eventId, payload);
        eventService.broadcastToDisplay(eventId, payload);

        return ResponseEntity.ok(Map.of("success", true, "showFeedback", newState));
    }

    @PostMapping("/reveal")
    public ResponseEntity<?> revealAnswers(@PathVariable Long eventId) {
        com.syed.QuizYa.model.Event event = eventService.getEventById(eventId).orElseThrow();
        event.setAnswersRevealed(true);
        eventService.saveEvent(event);

        // Also lock submissions for the current slide
        Map<String, Object> lockPayload = new HashMap<>();
        lockPayload.put("type", "LOCK_ANSWERS");
        eventService.broadcastToGuests(eventId, lockPayload);
        eventService.broadcastToDisplay(eventId, lockPayload);

        // Fetch current question to get the correct option(s)
        List<Long> correctOptionIds = new java.util.ArrayList<>();
        int currentIndex = event.getCurrentQuestionIndex();
        if (currentIndex > 0) {
            List<QuestionBank> banks = questionService.getEventBanks(eventId);
            if (!banks.isEmpty()) {
                List<Question> questions = questionService.getQuestionsForBank(banks.get(0).getId());
                if (currentIndex <= questions.size()) {
                    Question currentQuestion = questions.get(currentIndex - 1);
                    List<QuestionOption> options = questionService.getOptionsForQuestion(currentQuestion.getId());
                    for (QuestionOption opt : options) {
                        if (Boolean.TRUE.equals(opt.getCorrect())) {
                            correctOptionIds.add(opt.getId());
                        }
                    }
                }
            }
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "REVEAL_ANSWER");
        payload.put("correctOptionIds", correctOptionIds);
        eventService.broadcastToGuests(eventId, payload);
        eventService.broadcastToDisplay(eventId, payload);

        return ResponseEntity.ok(Map.of("success", true, "state", "REVEAL"));
    }

    /**
     * POST /finish
     *
     * Completes the event lifecycle by resetting the event to DRAFT mode.
     * Broadcasts an EVENT_FINISHED message so all clients show a final screen.
     * Clears all guest and answer data to allow the host to run the event again.
     */
    @PostMapping("/finish")
    public ResponseEntity<?> finishEvent(@PathVariable Long eventId) {
        // Broadcast the finish event to everyone so they know it's over
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "EVENT_FINISHED");
        eventService.broadcastToGuests(eventId, payload);
        eventService.broadcastToDisplay(eventId, payload);

        // Reset the event status to DRAFT
        eventService.finishEvent(eventId);
        
        // Clear out the guests and answers
        guestService.clearGuestsForEvent(eventId);

        return ResponseEntity.ok(Map.of("success", true, "state", "DRAFT"));
    }
}


