package com.springai.vector_store_api_basics.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_metadata")
public class DocumentMetadata {

    @Id
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String fileType;  // "PDF" or "TEXT"

    @Column(nullable = false)
    private Integer totalChunks;

    @Column(nullable = false)
    private Long fileSizeBytes;

    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String status; // "PROCESSING", "COMPLETED", "FAILED"

    // Default constructor
    public DocumentMetadata() {}

    // Constructor
    public DocumentMetadata(String filename, String fileType,
                            Integer totalChunks, Long fileSizeBytes) {
        this.filename = filename;
        this.fileType = fileType;
        this.totalChunks = totalChunks;
        this.fileSizeBytes = fileSizeBytes;
        this.status = "COMPLETED";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Integer getTotalChunks() { return totalChunks; }
    public void setTotalChunks(Integer totalChunks) { this.totalChunks = totalChunks; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}