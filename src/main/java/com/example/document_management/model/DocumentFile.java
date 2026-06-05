package com.example.document_management.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.time.Instant;

@Entity
@Table(name = "document_files")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentFile {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự")
    @Column(nullable = false, length = 255)
    private String title;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    @Column(length = 1000)
    private String description;

    
    @Column(length = 100)
    private String category;

    
    @Column(nullable = false, unique = true, length = 400)
    private String storedFilename;

    
    @Column(nullable = false, length = 400)
    private String originalFilename;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    
    @Column(nullable = false)
    private boolean archived;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        archived = false;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}

