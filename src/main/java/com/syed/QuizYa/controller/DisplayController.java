package com.syed.QuizYa.controller;

import com.syed.QuizYa.model.Event;
import com.syed.QuizYa.model.EventGuest;
import com.syed.QuizYa.service.EventService;
import com.syed.QuizYa.service.GuestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * CONTROLLER: DisplayController
 *
 * Serves the venue main display — the large screen projected at the event venue.
 *
 * For Week 3 this shows the lobby waiting screen (QR code + join code).
 * In Week 4-5 it will be extended to show live question slides and leaderboards
 * driven by WebSocket events.
 */
@Controller
public class DisplayController {

    private final EventService eventService;
    private final GuestService guestService;

    public DisplayController(EventService eventService, GuestService guestService) {
        this.eventService = eventService;
        this.guestService = guestService;
    }

    /**
     * GET /display/{joinCode}
     *
     * Loads the venue lobby page. The host opens this in a second browser tab
     * and projects it on the venue screen via projector / HDMI.
     *
     * Uses the join code (not the event ID) as the URL identifier so the URL
     * is human-readable and doesn't expose internal database IDs.
     */
    @GetMapping("/display/{joinCode}")
    public String showLobby(@PathVariable String joinCode, Model model) {
        Event event = eventService.getEventByJoinCode(joinCode)
                .orElseThrow(() -> new IllegalArgumentException("No event found for join code: " + joinCode));

        List<EventGuest> guests = guestService.getGuestsForEvent(event.getId());

        //  Put the data into a "Model" backpack, without this lobby.html will not understand event.
        model.addAttribute("event", event);
        model.addAttribute("joinCode", joinCode);
        model.addAttribute("guests", guests);
        model.addAttribute("guestCount", guests.size());

        //  Hand the backpack to the lobby.html template
        return "display/lobby";
    }

    @GetMapping("/display/{joinCode}/audience-preview")
    public String showAudiencePreview(@PathVariable String joinCode, Model model) {
        Event event = eventService.getEventByJoinCode(joinCode).orElseThrow();
        model.addAttribute("event", event);
        return "display/audience-preview";
    }
}
