// This line defines the "package" where this file belongs.
// A package is like a folder that groups related files together.
// Here, the package is called "com.springai.vector_store_api_basics.service".
package com.springai.vector_store_api_basics.service;

// Importing the VectorDocument entity class.
// This represents the "documents" table in the database.
import com.springai.vector_store_api_basics.model.VectorDocument;

// Importing the VectorRepository interface.
// This is used to perform database operations (like save, search) on the documents table.
import com.springai.vector_store_api_basics.repository.VectorRepository;

// Importing Spring AI classes.
// EmbeddingModel → generates embeddings (numerical vectors) for text.
// ChatModel → generates AI responses based on prompts.
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;

// Importing Spring annotations.
import org.springframework.beans.factory.annotation.Autowired; // Used to automatically inject dependencies.
import org.springframework.stereotype.Service;                // Marks this class as a Service in Spring.

// Importing Java utilities.
import java.util.List;
import java.util.stream.Collectors;

// @Service tells Spring:
// "This class contains business logic and should be managed as a Service bean."
@Service
public class VectorSearchService {

    // @Autowired tells Spring:
    // "Automatically inject the EmbeddingModel bean here."
    // EmbeddingModel converts text into numerical vectors (embeddings).
    @Autowired
    private EmbeddingModel embeddingModel;

    // @Autowired tells Spring:
    // "Automatically inject the ChatModel bean here."
    // ChatModel is used to generate AI responses (like Gemini or GPT).
    @Autowired
    private ChatModel chatModel;

    // @Autowired tells Spring:
    // "Automatically inject the VectorRepository bean here."
    // VectorRepository handles saving and searching documents in the database.
    @Autowired
    private VectorRepository vectorRepository;

    // Method to save a document into the vector store.
    // Steps:
    // 1. Convert the text content into an embedding vector using embeddingModel.embed().
    // 2. Create a new VectorDocument with the text and embedding.
    // 3. Save the document into the database using vectorRepository.save().
    public void saveDocument(String content) {
        float[] vector = embeddingModel.embed(content);
        vectorRepository.save(new VectorDocument(content, vector));
    }

    // Method to generate an AI answer based on user query and stored documents.
    public String generateAnswer(String userQuery) {
        // Step 1: Convert the user query into an embedding vector.
        float[] queryVector = embeddingModel.embed(userQuery);

        // Step 2: Convert the float[] vector into a comma-separated string
        // so it can be used in the SQL query for similarity search.
        String vectorStr = "[" + convertToCommaSeperated(queryVector) + "]";

        // Step 3: Search for the top 3 most similar documents in the database.
        List<VectorDocument> topDocs = vectorRepository.searchSimilar(vectorStr, 3);

        // Step 4: Collect the text content of those documents into a single string (context).
        String context = topDocs.stream()
                .map(VectorDocument::getText)
                .collect(Collectors.joining("\n"));

        // Step 5: Build a prompt for the AI model.
        // The prompt includes the retrieved context and the user’s query.
        String prompt = "Use the context below to answer:\n" + context + "\n\nQ: " + userQuery + "\nA:";

        // Step 6: Send the prompt to the AI model and return the generated answer.
        return chatModel.call(prompt);
    }

    // Helper method to convert a float[] vector into a comma-separated string.
    // Example: [0.12, -0.45, 0.89]
    private String convertToCommaSeperated(float[] vector) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) sb.append(",");
        }
        return sb.toString();
    }
}