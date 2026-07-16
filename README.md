Here's your complete README.md — just copy and paste directly into a file called README.md in your project root:
markdown# 🧠 DocMind AI — RAG Document Q&A System

> Chat with your documents intelligently using Google Gemini AI

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-green)
![Spring AI](https://img.shields.io/badge/Spring%20AI-1.1.2-blue)
![React](https://img.shields.io/badge/React-19-cyan)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-PGVector-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

## 🌐 Live Demo
**Frontend:** https://rag-frontend-kappa-ten.vercel.app

**Backend:** https://spring-ai-rag-pgvector-gemini-basics.onrender.com

---

## 📌 What is DocMind AI?

DocMind AI is a full-stack **RAG (Retrieval Augmented Generation)** based Document Q&A system built with **Spring Boot + Spring AI + Google Gemini**.

Upload any PDF → Ask questions in natural language → Get accurate AI answers based ONLY on your document.

### The Problem It Solves
Normal ChatGPT answers from its training data and can hallucinate.
DocMind AI answers ONLY from your uploaded documents — grounded, accurate, private.

---

## ✨ Features

- 📄 **PDF Upload** — Upload any PDF, automatically chunked and embedded
- 🔍 **Semantic Search** — Finds relevant content using cosine similarity on 3072-dim vectors
- 💬 **Persistent Chat Memory** — AI remembers previous questions within a session
- 🔐 **JWT Authentication** — Secure per-user document isolation
- 💡 **Related Questions** — AI suggests 3 follow-up questions after every answer
- 📁 **Document Management** — List and delete uploaded documents
- 🌐 **Fully Deployed** — Live on internet with cloud database

---

## 🏗️ Architecture
PDF Upload Flow:
─────────────────────────────────────────────────────
User uploads PDF
↓
PDFBox extracts text page by page
↓
Text split into ~800 char semantic chunks
↓
Google Gemini Embedding Model
generates 3072-dimensional vectors
↓
Vectors stored in PostgreSQL + PGVector
(hosted on Aiven Cloud)
Question Answering Flow:
─────────────────────────────────────────────────────
User asks a question
↓
Question converted to 3072-dim vector
↓
Cosine similarity search in PGVector
retrieves top 3 relevant chunks
↓
Last 10 chat messages + chunks + question
sent to Google Gemini 2.5 Flash
↓
Gemini generates grounded answer

3 related follow-up questions
↓
Answer saved to chat_messages table
↓
Response returned to user


---

## 🛠️ Tech Stack

| Layer | Technology | Why chosen |
|---|---|---|
| Backend | Spring Boot 3.5.9 | Production-grade Java framework |
| AI Framework | Spring AI 1.1.2 | Native Spring integration with LLMs |
| LLM | Google Gemini 2.5 Flash | Fast, free tier, high quality |
| Embeddings | Gemini Embedding 001 | 3072-dim high quality embeddings |
| Vector DB | PostgreSQL + PGVector | Vector search in existing DB, no extra infra |
| Cloud DB | Aiven | Managed PostgreSQL with PGVector |
| Security | Spring Security + JWT | Stateless auth, industry standard |
| PDF Processing | Apache PDFBox 3.0.2 | Reliable PDF text extraction |
| Frontend | React 19 | Modern, component-based UI |
| Backend Deploy | Render | Free cloud hosting |
| Frontend Deploy | GitHub Pages | Free static hosting |

---

## 📡 API Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | ❌ | Register new user |
| POST | `/api/auth/login` | ❌ | Login and get JWT token |
| POST | `/api/documents/upload` | ✅ | Upload PDF file |
| GET | `/api/documents` | ✅ | List all uploaded documents |
| DELETE | `/api/documents/{id}` | ✅ | Delete a document |
| POST | `/api/chat/ask` | ✅ | Ask question with chat memory |
| GET | `/api/chat/history/{sessionId}` | ✅ | Get session chat history |
| GET | `/api/chat/sessions` | ✅ | List all user sessions |
| DELETE | `/api/chat/sessions/{sessionId}` | ✅ | Delete a session |

---

## 🗄️ Database Schema

```sql
-- Users table
users (id, username, password, role)

-- Vector embeddings
documents (id, text, embedding vector(3072))

-- Document metadata
document_metadata (id, filename, fileType,
                   totalChunks, fileSizeBytes,
                   uploadedAt, status)

-- Chat history
chat_messages (id, username, sessionId,
               role, content, createdAt)
```

---

## 🚀 Run Locally

### Prerequisites
- Java 21+
- Maven
- PostgreSQL with PGVector extension
- Google Gemini API key (free at aistudio.google.com)

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/Anitaprajapati27/spring-ai-rag-pgvector-gemini-basics.git
cd spring-ai-rag-pgvector-gemini-basics
```

**2. Set environment variables**
```bash
# Windows PowerShell
$env:SPRING_AI_GOOGLE_GENAI_API_KEY="your_gemini_key"
$env:SPRING_AI_GOOGLE_GENAI_EMBEDDING_API_KEY="your_gemini_key"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://host:port/db?sslmode=require"
$env:SPRING_DATASOURCE_USERNAME="your_db_username"
$env:SPRING_DATASOURCE_PASSWORD="your_db_password"
$env:JWT_SECRET="your-secret-key-min-32-characters-long"
```

**3. Run the backend**
```bash
./mvnw spring-boot:run
```

**4. Run the frontend**
```bash
cd ../rag-frontend
npm install
npm start
```

**5. Open in browser**
http://localhost:3000

---

## 🔐 How Authentication Works

POST /api/auth/register → create account
POST /api/auth/login → get JWT token
Add token to every request:
Authorization: Bearer <your-token>
Server validates token → identifies user
User only sees their own documents


---

## 🎯 Key Technical Decisions

**Why Spring AI instead of Python LangChain?**
Spring AI uses familiar Spring conventions (Dependency Injection,
auto-configuration) making it production-ready for enterprise Java
teams. Very rare skill — differentiates from Python-based projects.

**Why PGVector instead of Pinecone?**
PGVector adds vector search to existing PostgreSQL, eliminating the
need for a separate vector database. Simpler architecture, lower cost.

**Why Google Gemini instead of OpenAI?**
Gemini API has a generous free tier with no credit card required.
gemini-embedding-001 produces high quality 3072-dimensional embeddings.

**Why page-based chunking?**
Page-based chunking preserves document structure and context better
than fixed character-size chunking for most real-world PDFs.


## 🤝 Connect
**Anita Prajapati**
- 🔗 GitHub: [@Anitaprajapati27](https://github.com/Anitaprajapati27)
---

## 📄 License

MIT License — free to use as reference for learning!
