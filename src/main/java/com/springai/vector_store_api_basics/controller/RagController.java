// This line defines the "package" where this file belongs.
// A package is like a folder that groups related files together.
// Here, the package is called "com.springai.vector_store_api_basics.controller".
package com.springai.vector_store_api_basics.controller;

// Importing the VectorSearchService class.
// This contains the business logic for saving documents and generating answers using vector search + AI.
import com.springai.vector_store_api_basics.service.VectorSearchService;

// Importing Spring annotations and classes for building REST APIs.
import org.springframework.beans.factory.annotation.Autowired;   // Used to automatically inject dependencies.
import org.springframework.web.bind.annotation.PostMapping;     // Maps HTTP POST requests to a method.
import org.springframework.web.bind.annotation.RequestBody;     // Binds the request JSON body to a Java object or map.
import org.springframework.web.bind.annotation.RequestMapping;  // Maps a base URL to this controller.
import org.springframework.web.bind.annotation.RestController;  // Marks this class as a REST controller.

// Importing Java utilities.
import java.util.Map;

// @RestController tells Spring:
// "This class will handle REST API requests and return responses directly (usually JSON or text)."
@RestController

// @RequestMapping("/api/rag") means:
// All URLs handled by this controller will start with "/api/rag".
// Example: http://localhost:8080/api/rag/add
@RequestMapping("/api/rag")
public class RagController {

    // @Autowired tells Spring:
    // "Automatically inject the VectorSearchService bean here."
    // This allows us to call methods from VectorSearchService without manually creating an object.
    @Autowired
    private VectorSearchService ragService;

    // @PostMapping("/add") means:
    // This method will run when the user sends a POST request to "/api/rag/add".
    // Example: POST http://localhost:8080/api/rag/add
    //
    // @RequestBody Map<String,String> body:
    // - Takes the request body (JSON) and binds it to a Map.
    // - Example request body: { "content": "Spring AI integrates with vector search." }
    //
    // The method then calls ragService.saveDocument(content),
    // which saves the document text + embedding into the database.
    // Finally, it returns a confirmation message.
    @PostMapping("/add")
    public String addData(@RequestBody Map<String,String> body) {
        ragService.saveDocument(body.get("content"));
        return "Added to Aiven DB!";
    }

    // @PostMapping("/ask") means:
    // This method will run when the user sends a POST request to "/api/rag/ask".
    // Example: POST http://localhost:8080/api/rag/ask
    //
    // @RequestBody Map<String,String> body:
    // - Takes the request body (JSON) and binds it to a Map.
    // - Example request body: { "question": "What does Spring AI do?" }
    //
    // The method then calls ragService.generateAnswer(question),
    // which performs vector similarity search + AI response generation.
    // Finally, it returns the AI-generated answer.
    @PostMapping("/ask")
    public String ask(@RequestBody Map<String,String> body) {
        return ragService.generateAnswer(body.get("question"));
    }
}