package com.football.booking_system.controller;

import com.football.booking_system.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin("*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");
        String response = chatService.processMessage(message);
        return ResponseEntity.ok(Map.of("reply", response));
    }
}