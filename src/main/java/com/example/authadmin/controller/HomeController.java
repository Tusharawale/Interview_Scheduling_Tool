package com.example.authadmin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        // Serve the login page when hitting http://localhost:8080
        return "redirect:/login.html";
    }
}

