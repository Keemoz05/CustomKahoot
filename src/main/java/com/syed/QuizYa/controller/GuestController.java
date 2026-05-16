package com.syed.QuizYa.controller;

import com.syed.QuizYa.model.EventGuest;
import com.syed.QuizYa.service.GuestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GuestController {

    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @GetMapping("/join")
    public String showJoinPage(@RequestParam(required = false) String pin, Model model) {
        model.addAttribute("pin", pin);
        return "guest/join";
    }

    @PostMapping("/join")
    public String handleJoin(@RequestParam String pin, @RequestParam String displayName, Model model) {
        try {
            EventGuest guest = guestService.joinEvent(pin, displayName);
            return "redirect:/play?token=" + guest.getSessionToken();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("pin", pin);
            return "guest/join";
        }
    }

    @GetMapping("/play")
    public String showPlayPage(@RequestParam(required = false) String token, Model model) {
        if (token == null || token.isEmpty()) {
            return "guest/play-recover";
        }
        EventGuest guest = guestService.getGuestByToken(token);
        model.addAttribute("guest", guest);
        return "guest/play";
    }
}
