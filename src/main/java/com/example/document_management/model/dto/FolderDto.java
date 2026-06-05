package com.example.document_management.model.dto;

public class FolderDto {
    private String name;
    private long documentCount;

    public FolderDto() {}
    public FolderDto(String name, long documentCount) { this.name = name; this.documentCount = documentCount; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getDocumentCount() { return documentCount; }
    public void setDocumentCount(long documentCount) { this.documentCount = documentCount; }
}



