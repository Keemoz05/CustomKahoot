package com.syed.QuizYa.controller;

import com.syed.QuizYa.model.MediaAsset;
import com.syed.QuizYa.model.Question;
import com.syed.QuizYa.model.QuestionBank;
import com.syed.QuizYa.model.QuestionOption;
import com.syed.QuizYa.repository.QuestionRepository;
import com.syed.QuizYa.service.EventService;
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

    // Reads server.port from application.properties to build the base URL for the QR code
    @Value("${server.port:8080}")
    private int serverPort;

    public HostApiController(QuestionService questionService,
                             EventService eventService,
                             MediaService mediaService,
                             QuestionRepository questionRepository) {
        this.questionService = questionService;
        this.eventService = eventService;
        this.mediaService = mediaService;
        this.questionRepository = questionRepository;
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
        questionService.updateQuestion(qId, type, prompt);
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
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
