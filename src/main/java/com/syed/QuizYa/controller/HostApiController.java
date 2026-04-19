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

    // Reads server.port from application.properties to build the base URL for the QR code
    @Value("${server.port:8080}")
    private int serverPort;

    public HostApiController(QuestionService questionService,
                             EventService eventService,
                             MediaService mediaService,
                             QuestionRepository questionRepository,
                             GuestService guestService) {
        this.questionService = questionService;
        this.eventService = eventService;
        this.mediaService = mediaService;
        this.questionRepository = questionRepository;
        this.guestService = guestService;
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

    // ─── Answer Options ──────────────────────────────────────────────────────

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

    /**
     * POST /go-live
     *
     * Transitions the event from DRAFT → LOBBY, generates a QR code, and returns
     * the data needed to show the host the join info overlay.
     *
     * Uses HttpServletRequest to derive the base URL dynamically (works on any port/host).
     */
    @PostMapping("/go-live")
    public ResponseEntity<?> goLive(@PathVariable Long eventId, HttpServletRequest request) {
        try {
            // Build base URL using local IP instead of localhost so phones can scan QR and join
            String serverName;
            try {
                serverName = java.net.InetAddress.getLocalHost().getHostAddress();
            } catch (java.net.UnknownHostException e) {
                serverName = request.getServerName(); // Fallback if IP cannot be resolved
            }
            String baseUrl = request.getScheme() + "://" + serverName
                    + ":" + request.getServerPort();

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
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    // ─── Live Controls (Week 5) ───────────────────────────────────────────────
    //
    // These five endpoints implement the host's real-time game pacing controls
    // (Use Cases H6, H7, S1).  The intended flow is:
    //
    //   1. Host clicks "Next Slide"  → /next   → broadcasts SHOW_QUESTION
    //   2. Host clicks "Lock"        → /lock   → broadcasts LOCK_ANSWERS
    //   3. Host clicks "Reveal"      → /reveal → broadcasts REVEAL_ANSWER
    //   4. Host clicks "Leaderboard" → /leaderboard → broadcasts SHOW_LEADERBOARD
    //                                              + private RANK_UPDATE per guest
    //   5. Host toggles pause        → /pause  → broadcasts PAUSE / RESUME
    //
    // Every endpoint constructs a JSON payload with a "type" key and sends it
    // to two WebSocket topics:
    //   - /topic/event/{id}/guest  → all guest play.html pages
    //   - /topic/event/{id}/lobby  → the venue display screen (lobby.html)
    // The frontends switch their UI state based on the "type" value.
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * POST /next
     *
     * Advances the event to the next question.  Increments the event's
     * currentQuestionIndex (1-based), loads that question + its options from
     * the event's default QuestionBank, and broadcasts a SHOW_QUESTION payload
     * containing the prompt text and answer options to both guests and the
     * venue display.
     *
     * Returns 400 if there are no more questions left in the bank.
     */
    @PostMapping("/next")
    public ResponseEntity<?> nextQuestion(@PathVariable Long eventId) {
        com.syed.QuizYa.model.Event event = eventService.getEventById(eventId).orElseThrow();

        // Increment the 1-based question pointer stored on the Event entity.
        // currentQuestionIndex starts at 0 (no question shown yet).
        event.setCurrentQuestionIndex(event.getCurrentQuestionIndex() + 1);

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

        // TODO: persist the updated currentQuestionIndex properly.
        // Currently calling goLive() is a hack that only works once because
        // goLive() checks for DRAFT status.  A dedicated saveEvent() method
        // should be added to EventService in a future pass.

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

    /**
     * POST /lock
     *
     * Prevents guests from submitting any more answers for the current question.
     * The guest's play.html listens for LOCK_ANSWERS but doesn't currently
     * have explicit lock handling — in practice guests are already in the
     * "waiting" feedback state after submitting.  The venue display updates
     * its prompt text to "Answers Locked!".
     */
    @PostMapping("/lock")
    public ResponseEntity<?> lockAnswers(@PathVariable Long eventId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "LOCK_ANSWERS");
        eventService.broadcastToGuests(eventId, payload);
        eventService.broadcastToDisplay(eventId, payload);
        return ResponseEntity.ok(Map.of("success", true, "state", "LOCKED"));
    }

    /**
     * POST /reveal
     *
     * Tells all clients that the correct answer can now be shown.
     *
     * On the guest side (play.html), the REVEAL_ANSWER event triggers the
     * deferred feedback display: when the guest submitted their answer, the
     * API response included { correct: true/false } which was stashed in
     * window.lastAnswerCorrect.  The reveal event reads that stashed value
     * and shows ✅ or ❌ accordingly.
     *
     * On the venue display, the prompt text changes to "Correct Answer Revealed".
     */
    @PostMapping("/reveal")
    public ResponseEntity<?> revealAnswers(@PathVariable Long eventId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "REVEAL_ANSWER");
        eventService.broadcastToGuests(eventId, payload);
        eventService.broadcastToDisplay(eventId, payload);

        return ResponseEntity.ok(Map.of("success", true, "state", "REVEAL"));
    }

    /**
     * POST /leaderboard
     *
     * Two things happen here:
     *
     * 1. PUBLIC broadcast — the top 5 guests (sorted by correctCount DESC)
     *    are sent to both WebSocket channels as a SHOW_LEADERBOARD payload.
     *    The venue display renders them as a numbered list; guests see a
     *    "Leaderboard" state on their phone.
     *
     * 2. PRIVATE per-guest broadcast — for every guest in the event, we
     *    calculate their personal rank (position in the sorted list) and
     *    send a RANK_UPDATE message to their private queue at
     *    /queue/guest/{guestId}.  The guest's play.html picks this up in
     *    handleGuestMessage() and updates the rank card and correct counter
     *    on their device.
     *
     * Scoring is accuracy-based only — ranking is purely by number of
     * correct answers (correctCount).  There is NO speed bonus, matching
     * the design requirement for equal marks regardless of response time.
     */
    @PostMapping("/leaderboard")
    public ResponseEntity<?> showLeaderboard(@PathVariable Long eventId) {
        // Fetch the top 5 guests sorted by correctCount descending.
        List<com.syed.QuizYa.model.EventGuest> topGuests = guestService.getTopGuests(eventId, 5);
        List<Map<String, Object>> topList = topGuests.stream().map(g -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", g.getDisplayName());
            map.put("score", g.getCorrectCount());  // "score" is just the raw correct count
            return map;
        }).collect(Collectors.toList());

        // Build and broadcast the public leaderboard payload.
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "SHOW_LEADERBOARD");
        payload.put("topGuests", topList);

        eventService.broadcastToGuests(eventId, payload);
        eventService.broadcastToDisplay(eventId, payload);

        // Send a private RANK_UPDATE to each individual guest.
        // This uses /queue/guest/{guestId} which is a per-user destination,
        // so each guest only sees their own rank — not anyone else's.
        List<com.syed.QuizYa.model.EventGuest> guests = guestService.getGuestsForEvent(eventId);
        for (com.syed.QuizYa.model.EventGuest guest : guests) {
            int rank = guestService.calculateRank(eventId, guest.getId());
            Map<String, Object> rankPayload = new HashMap<>();
            rankPayload.put("type", "RANK_UPDATE");
            rankPayload.put("rank", rank);
            rankPayload.put("correctCount", guest.getCorrectCount());
            eventService.broadcastToGuest(guest.getId(), rankPayload);
        }

        return ResponseEntity.ok(Map.of("success", true, "state", "LEADERBOARD"));
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

    /**
     * POST /pause
     *
     * Implements the "Storyteller Pause / Hold" toggle (Use Case H6).
     * When activated, the venue display shows a full-screen dark overlay
     * with "Eyes to the front!" and the guest's phone switches to a
     * calming "Paused" waiting state.  This freezes the visual game state
     * so the host can address the audience without distraction.
     *
     * The toggle is stateless on the server — the frontend tracks
     * isPaused and sends { paused: true/false } with each click.
     */
    @PostMapping("/pause")
    public ResponseEntity<?> togglePause(@PathVariable Long eventId, @RequestBody Map<String, Boolean> body) {
        Boolean paused = body.getOrDefault("paused", true);
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", paused ? "PAUSE" : "RESUME");
        eventService.broadcastToGuests(eventId, payload);
        eventService.broadcastToDisplay(eventId, payload);
        return ResponseEntity.ok(Map.of("success", true, "paused", paused));
    }
}
