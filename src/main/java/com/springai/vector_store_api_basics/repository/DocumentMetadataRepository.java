package com.springai.vector_store_api_basics.repository;

import com.springai.vector_store_api_basics.model.DocumentMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentMetadataRepository
        extends JpaRepository<DocumentMetadata, String> {

    List<DocumentMetadata> findAllByOrderByUploadedAtDesc();
    List<DocumentMetadata> findByFileType(String fileType);
}