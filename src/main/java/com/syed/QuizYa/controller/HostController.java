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
}
