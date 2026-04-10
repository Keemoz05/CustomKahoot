package com.syed.QuizYa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GuestController {

    @GetMapping("/join")
    public String showJoinPage() {
        return "index";
    }
}
