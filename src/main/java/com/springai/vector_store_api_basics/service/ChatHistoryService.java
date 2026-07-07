package com.springai.vector_store_api_basics.service;

import com.springai.vector_store_api_basics.model.ChatMessage;
import com.springai.vector_store_api_basics.repository.ChatMessageRepository;
import com.springai.vector_store_api_basics.repository.VectorRepository;
import com.springai.vector_store_api_basics.model.VectorDocument;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatHistoryService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private VectorRepository vectorRepository;

    @Autowired
    private ChatModel chatModel;

    // ─────────────────────────────────────────
    // Main method — ask question with memory
    // ─────────────────────────────────────────
    public String askWithHistory(String username,
                                  String sessionId,
                                  String userQuestion) {

        // Step 1: Save user's question to DB
        chatMessageRepository.save(new ChatMessage(
                username, sessionId, "USER", userQuestion));

        // Step 2: Get last 10 messages for context
        List<ChatMessage> recentMessages =
            chatMessageRepository
                .findTop10ByUsernameAndSessionIdOrderByCreatedAtDesc(
                    username, sessionId);
        // Reverse to get chronological order
        Collections.reverse(recentMessages);

        // Step 3: Search vector store for relevant chunks
        float[] queryVector = embeddingModel.embed(userQuestion);
        String vectorStr = "[" + convertToString(queryVector) + "]";
        List<VectorDocument> topDocs =
            vectorRepository.searchSimilar(vectorStr, 3);

        String context = topDocs.stream()
                .map(VectorDocument::getText)
                .collect(Collectors.joining("\n"));

        // Step 4: Build messages list with history
        List<Message> messages = new ArrayList<>();

        // System prompt
        messages.add(new SystemMessage("""
            You are a helpful assistant that answers questions
            based on the provided document context.
            If the answer is not in the context, say
            'I could not find this in the uploaded documents.'
            Use the conversation history to understand
            follow-up questions.
            
            Context from documents:
            """ + context));

        // Add conversation history
        for (ChatMessage msg : recentMessages) {
            if (msg.getRole().equals("USER")) {
                messages.add(new UserMessage(msg.getContent()));
            } else {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }

        // Step 5: Call Gemini with full conversation
        Prompt prompt = new Prompt(messages);
        String answer = chatModel.call(prompt)
                .getResult()
                .getOutput()
                .getText();

        // Step 6: Save AI response to DB
        chatMessageRepository.save(new ChatMessage(
                username, sessionId, "ASSISTANT", answer));

        return answer;
    }

    // ─────────────────────────────────────────
    // Get full chat history for a session
    // ─────────────────────────────────────────
    public List<ChatMessage> getSessionHistory(String username,
                                                String sessionId) {
        return chatMessageRepository
            .findByUsernameAndSessionIdOrderByCreatedAtAsc(
                username, sessionId);
    }

    // ─────────────────────────────────────────
    // Get all sessions for a user
    // ─────────────────────────────────────────
    public List<String> getUserSessions(String username) {
        return chatMessageRepository
            .findByUsernameOrderByCreatedAtDesc(username)
            .stream()
            .map(ChatMessage::getSessionId)
            .distinct()
            .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // Delete a session
    // ─────────────────────────────────────────
    @Transactional
    public void deleteSession(String username, String sessionId) {
        chatMessageRepository
            .deleteByUsernameAndSessionId(username, sessionId);
    }

    // Helper
    private String convertToString(float[] vector) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) sb.append(",");
        }
        return sb.toString();
    }
}