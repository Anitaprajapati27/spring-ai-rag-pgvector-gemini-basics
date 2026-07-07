package com.springai.vector_store_api_basics.repository;

import com.springai.vector_store_api_basics.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, Long> {

    // Get all messages for a session in order
    List<ChatMessage> findByUsernameAndSessionIdOrderByCreatedAtAsc(
            String username, String sessionId);

    // Get all sessions for a user
    List<ChatMessage> findByUsernameOrderByCreatedAtDesc(
            String username);

    // Get last N messages for context
    List<ChatMessage> findTop10ByUsernameAndSessionIdOrderByCreatedAtDesc(
            String username, String sessionId);

    // Delete all messages for a session
    void deleteByUsernameAndSessionId(
            String username, String sessionId);
}