package com.example.document_management.model.mapper;

import com.example.document_management.model.DocumentFile;
import com.example.document_management.model.dto.DocumentDto;

public final class DocumentMapper {
    private DocumentMapper() {}

    public static DocumentDto toDto(DocumentFile e) {
        DocumentDto d = new DocumentDto();
        d.setId(e.getId());
        d.setTitle(e.getTitle());
        d.setDescription(e.getDescription());
        d.setCategory(e.getCategory());
        d.setOriginalFilename(e.getOriginalFilename());
        d.setStoredFilename(e.getStoredFilename());
        d.setCreatedAt(e.getCreatedAt());
        d.setUpdatedAt(e.getUpdatedAt());
        d.setArchived(e.isArchived());
        return d;
    }
}



