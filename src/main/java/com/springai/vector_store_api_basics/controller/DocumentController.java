package com.springai.vector_store_api_basics.controller;

import com.springai.vector_store_api_basics.model.DocumentMetadata;
import com.springai.vector_store_api_basics.repository.DocumentMetadataRepository;
import com.springai.vector_store_api_basics.service.PdfIngestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    @Autowired
    private PdfIngestionService pdfIngestionService;

    @Autowired
    private DocumentMetadataRepository metadataRepository;

    // Upload PDF
    @PostMapping("/upload")
    public ResponseEntity<?> uploadPdf(
            @RequestParam("file") MultipartFile file) {
        try {
            DocumentMetadata metadata = pdfIngestionService.ingestPdf(file);
            return ResponseEntity.ok(Map.of(
                "message", "✅ PDF uploaded and processed successfully!",
                "filename", metadata.getFilename(),
                "chunks", metadata.getTotalChunks(),
                "sizeBytes", metadata.getFileSizeBytes(),
                "uploadedAt", metadata.getUploadedAt().toString()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to process PDF: "
                    + e.getMessage()));
        }
    }

    // List all uploaded documents
    @GetMapping
    public ResponseEntity<List<DocumentMetadata>> getAllDocuments() {
        return ResponseEntity.ok(
            metadataRepository.findAllByOrderByUploadedAtDesc()
        );
    }

    // Delete a document by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable String id) {
        if (!metadataRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        metadataRepository.deleteById(id);
        return ResponseEntity.ok(
            Map.of("message", "Document deleted successfully")
        );
    }
}