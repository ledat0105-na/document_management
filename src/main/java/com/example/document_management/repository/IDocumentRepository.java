package com.example.document_management.repository;

import com.example.document_management.model.DocumentFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IDocumentRepository extends JpaRepository<DocumentFile, Long> {
    Optional<DocumentFile> findByStoredFilename(String storedFilename);
    boolean existsByStoredFilename(String storedFilename);
}

