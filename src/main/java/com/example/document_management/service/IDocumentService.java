package com.example.document_management.service;

import com.example.document_management.model.DocumentFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IDocumentService {
    Page<DocumentFile> list(String q, Pageable pageable);
    DocumentFile create(DocumentFile meta, String originalFilename, String storedFilename);
    DocumentFile update(Long id, String title, String description);
    void delete(Long id);
    Optional<DocumentFile> findById(Long id);
}

