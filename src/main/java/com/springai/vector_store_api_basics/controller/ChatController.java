package com.springai.vector_store_api_basics.controller;

import com.springai.vector_store_api_basics.model.ChatMessage;
import com.springai.vector_store_api_basics.service.ChatHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatHistoryService chatHistoryService;

    // Ask question with memory
    @PostMapping("/ask")
    public ResponseEntity<?> ask(
            Authentication auth,
            @RequestBody Map<String, String> body) {
        try {
            String username = auth.getName();
            String question = body.get("question");

            // Use provided sessionId or generate new one
            String sessionId = body.getOrDefault(
                "sessionId", UUID.randomUUID().toString());

            if (question == null || question.isBlank()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Question cannot be empty"));
            }

            String answer = chatHistoryService.askWithHistory(
                username, sessionId, question);

            return ResponseEntity.ok(Map.of(
                "answer", answer,
                "sessionId", sessionId,
                "username", username
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }

    // Get chat history for a session
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> getHistory(
            Authentication auth,
            @PathVariable String sessionId) {
        String username = auth.getName();
        return ResponseEntity.ok(
            chatHistoryService.getSessionHistory(username, sessionId));
    }

    // Get all sessions for logged-in user
    @GetMapping("/sessions")
    public ResponseEntity<List<String>> getSessions(
            Authentication auth) {
        return ResponseEntity.ok(
            chatHistoryService.getUserSessions(auth.getName()));
    }

    // Delete a session
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> deleteSession(
            Authentication auth,
            @PathVariable String sessionId) {
        chatHistoryService.deleteSession(auth.getName(), sessionId);
        return ResponseEntity.ok(
            Map.of("message", "Session deleted successfully"));
    }
}