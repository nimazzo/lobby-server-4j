package com.example.lobbyserver.home;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping(produces = "text/plain")
    public String home() {
        return "Welcome to the Lobby Server!";
    }
}
