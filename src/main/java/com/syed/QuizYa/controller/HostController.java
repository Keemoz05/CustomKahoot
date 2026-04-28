package com.syed.QuizYa.controller;

import com.syed.QuizYa.model.Event;
import com.syed.QuizYa.service.EventService;
import com.syed.QuizYa.service.QuestionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/host")
public class HostController {

    private final EventService eventService;
    private final QuestionService questionService;

    public HostController(EventService eventService, QuestionService questionService) {
        this.eventService = eventService;
        this.questionService = questionService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("events", eventService.getAllEvents());
        return "host/dashboard";
    }

    @PostMapping("/events")
    public String createEvent(@RequestParam String title, @RequestParam(required = false) String description) {
        Event event = eventService.createEvent(title, description);
        return "redirect:/host/events/" + event.getId() + "/setup";
    }

    @GetMapping("/events/{id}/setup")
    public String showEventSetup(@PathVariable Long id, Model model) {
        Event event = eventService.getEventById(id).orElseThrow(() -> new IllegalArgumentException("Invalid event Id:" + id));
        model.addAttribute("event", event);
        return "host/setup";
    }

    /**
     * GET /host/events/{id}/live
     *
     * Week 5: Serves the Host Live Controls page.  This is a separate view
     * from the setup page — it shows four game-pacing buttons (Next, Lock,
     * Reveal, Leaderboard), a Storyteller Pause toggle, and live stats
     * (guest count, answer count).
     *
     * The host navigates here after clicking "Go Live" on the setup page.
     * The setup overlay shows a "Live Controls" link that points to this URL.
     */
    @GetMapping("/events/{id}/live")
    public String showLiveControls(@PathVariable Long id, Model model) {
        Event event = eventService.getEventById(id).orElseThrow(() -> new IllegalArgumentException("Invalid event Id:" + id));
        model.addAttribute("event", event);
        model.addAttribute("joinCode", event.getJoinCode());
        return "host/live";
    }
}
