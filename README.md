#  RAG Basics: Spring AI + PostgreSQL PGVector

## Project Overview
Standard AI models are limited to the data they were trained on. **Retrieval-Augmented Generation (RAG)** solves this by:
1.  **Retrieving** relevant facts from your own database (PostgreSQL).
2.  **Augmenting** the user's prompt with those facts.
3.  **Generating** a response that is grounded in your private data.

This project specifically uses **PGVector**, an extension that turns a standard PostgreSQL database into a powerful vector engine.



---

##  The Core Concept: What is RAG?

Normally, an AI like Gemini only knows what it was trained on. It doesn't know your private data or recent notes. **RAG** solves this by:
1.  **Storing** your data as "Embeddings" (math vectors) in a database.
2.  **Searching** for the most relevant pieces of data when you ask a question.
3.  **Feeding** that relevant data (context) to the AI to help it answer accurately.

---

##  Tech Stack & Tools
* **Java 25**: Leveraging the latest LTS features.
* **Spring Boot 3.5.9**: The backbone of the API.
* **Spring AI 1.1.2**: The framework connecting Java to AI models.
* **Google Gemini 2.5 Flash**: The LLM for generating answers.
* **Text-Embedding-004**: Google's model that turns text into 768-dimension vectors.
* **PostgreSQL + PGVector**: A relational database that can perform "similarity searches" using vectors.

---

## Dependencies Needed (Used)
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-advisors-vector-store</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-google-genai</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-vector-store-pgvector</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-google-genai-embedding</artifactId>
    </dependency>

    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-vector</artifactId>
        <version>6.6.2.Final</version> </dependency>

    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>


    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
</dependencies>
```

##  Technical Concepts

### 1. Embeddings: The AI's Language
Computers don't understand words; they understand numbers.
* **Embedding Model**: We use `text-embedding-004` from Google. It converts a sentence like "The sky is blue" into a **768-dimensional vector** (a list of 768 numbers).
* **Semantic Meaning**: Sentences with similar meanings will have vectors that are mathematically "close" to each other in space.

### 2. PGVector & Similarity Search
Standard SQL uses `WHERE name = 'John'`. Vector databases use **Cosine Distance** (`<=>` in PGVector).
* Our custom query in `VectorRepository` finds documents by calculating which vectors in the database are most similar to the user's question vector.



### 3. The RAG Workflow
1.  **Ingestion (`/add`)**: Text is converted to a vector and stored in PostgreSQL.
2.  **Query (`/ask`)**:
    - The user's question is converted to a vector.
    - We search PostgreSQL for the Top-3 most relevant document chunks.
    - We "stuff" those chunks into the prompt.
    - Gemini answers based *only* on that context.

---

##  Component Reference

### `VectorDocument.java`
This is the blueprint of how our data looks in PostgreSQL.
* **Text**: The actual content you want to remember.
* **Embedding**: A `float[]` array of **768** numbers. This is the "mathematical fingerprint" of the text.
* **@JdbcTypeCode(SqlTypes.VECTOR)**: Tells Hibernate to treat this array as a PGVector type.

A JPA Entity that uses `@JdbcTypeCode(SqlTypes.VECTOR)`. This allows Hibernate to talk to the special `vector` column type in PostgreSQL.

### `VectorRepository.java`
This is where the magic search happens. It uses a **Native Query**:
```sql
SELECT id, text, embedding FROM documents 
ORDER BY embedding <=> cast(:queryVector AS vector) 
LIMIT :topK
```
Uses a **Native Query** to perform the similarity search. The `<=>` operator is specific to PGVector and tells the database to "order by similarity to this vector."

### `VectorSearchService.java`
The orchestrator. It handles the two-step process:
1. Converting text to vectors using `EmbeddingModel`.
2. Building the final prompt for the `ChatModel`.

---

##  How to Test

1.  **Database**: Ensure your PostgreSQL instance has the PGVector extension installed (`CREATE EXTENSION vector;`).
2.  **Config**: Update your Aiven PostgreSQL credentials and Google API Key in `application.properties`.
3.  **Step 1: Teach the AI**:
    - **POST** to `/api/rag/add`
    - **Body**: `{"content": "The company policy states that Friday is Pizza Day."}`
4.  **Step 2: Ask the AI**:
    - **POST** to `/api/rag/ask`
    - **Body**: `{"question": "What happens on Friday?"}`
    - **Result**: "According to the company policy, Friday is Pizza Day."