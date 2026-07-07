package com.springai.vector_store_api_basics.service;

import com.springai.vector_store_api_basics.model.DocumentMetadata;
import com.springai.vector_store_api_basics.model.VectorDocument;
import com.springai.vector_store_api_basics.repository.DocumentMetadataRepository;
import com.springai.vector_store_api_basics.repository.VectorRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfIngestionService {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private VectorRepository vectorRepository;

    @Autowired
    private DocumentMetadataRepository metadataRepository;

    // ─────────────────────────────────────────────
    // Main entry point — called by DocumentController
    // ─────────────────────────────────────────────
    public DocumentMetadata ingestPdf(MultipartFile file) throws IOException {

        // Step 1: Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException(
                "Only PDF files are supported");
        }

        // Step 2: Extract text from PDF page by page into chunks
        List<String> chunks = extractChunksFromPdf(file);

        if (chunks.isEmpty()) {
            throw new IllegalArgumentException(
                "Could not extract text from PDF — file may be scanned/image-only");
        }

        System.out.println("📄 Extracted " + chunks.size()
            + " chunks from: " + file.getOriginalFilename());

        // Step 3: Embed each chunk and save to vector store
        List<VectorDocument> vectorDocs = new ArrayList<>();
        for (String chunk : chunks) {
            // Skip very short chunks — not useful for search
            if (chunk != null && !chunk.trim().isEmpty()
                    && chunk.trim().length() > 20) {
                float[] embedding = embeddingModel.embed(chunk.trim());
                vectorDocs.add(new VectorDocument(chunk.trim(), embedding));
                System.out.println("✅ Embedded chunk "
                    + vectorDocs.size() + "/" + chunks.size());
            }
        }

        // Step 4: Save all embeddings to PostgreSQL
        vectorRepository.saveAll(vectorDocs);
        System.out.println("💾 Saved " + vectorDocs.size()
            + " embeddings to database");

        // Step 5: Save document metadata
        DocumentMetadata metadata = new DocumentMetadata(
            file.getOriginalFilename(),
            "PDF",
            vectorDocs.size(),
            file.getSize()
        );
        return metadataRepository.save(metadata);
    }

    // ─────────────────────────────────────────────
    // Extract text page by page — memory efficient
    // ─────────────────────────────────────────────
    private List<String> extractChunksFromPdf(MultipartFile file)
            throws IOException {

        List<String> chunks = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();

            System.out.println("📖 Total pages: " + totalPages);

            // Process 2 pages at a time as one chunk
            int pagesPerChunk = 2;
            for (int i = 1; i <= totalPages; i += pagesPerChunk) {
                stripper.setStartPage(i);
                stripper.setEndPage(
                    Math.min(i + pagesPerChunk - 1, totalPages));

                String pageText = stripper.getText(document);

                if (pageText != null && !pageText.trim().isEmpty()) {
                    // Further split if chunk is too large
                    if (pageText.length() > 1000) {
                        chunks.addAll(splitLargeText(pageText));
                    } else {
                        chunks.add(pageText);
                    }
                }
            }
        }
        return chunks;
    }

    // ─────────────────────────────────────────────
    // Split large text into sentence-aware chunks
    // ─────────────────────────────────────────────
    private List<String> splitLargeText(String text) {
        List<String> parts = new ArrayList<>();
        // Split on sentence boundaries
        String[] sentences = text.split("(?<=[.!?])\\s+");
        StringBuilder current = new StringBuilder();

        for (String sentence : sentences) {
            // If adding this sentence exceeds limit, save current and start new
            if (current.length() + sentence.length() > 800) {
                if (current.length() > 0) {
                    parts.add(current.toString().trim());
                    current = new StringBuilder();
                }
                // If single sentence is too long, split by words
                if (sentence.length() > 800) {
                    parts.addAll(splitByWords(sentence, 800));
                    continue;
                }
            }
            current.append(sentence).append(" ");
        }

        // Add remaining text
        if (current.length() > 0) {
            parts.add(current.toString().trim());
        }
        return parts;
    }

    // ─────────────────────────────────────────────
    // Fallback — split by word count if no sentences
    // ─────────────────────────────────────────────
    private List<String> splitByWords(String text, int maxChars) {
        List<String> parts = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            if (current.length() + word.length() + 1 > maxChars) {
                if (current.length() > 0) {
                    parts.add(current.toString().trim());
                    current = new StringBuilder();
                }
            }
            current.append(word).append(" ");
        }

        if (current.length() > 0) {
            parts.add(current.toString().trim());
        }
        return parts;
    }
}