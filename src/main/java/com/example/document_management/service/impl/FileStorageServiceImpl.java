package com.example.document_management.service.impl;

import com.example.document_management.service.IFileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements IFileStorageService {

    private final Path root;

    public FileStorageServiceImpl(@Value("${app.storage.root:storage}") String storageRoot) throws IOException {
        this.root = Paths.get(storageRoot).toAbsolutePath().normalize();
        Files.createDirectories(this.root);
    }

    @Override
    public String store(MultipartFile file) throws IOException {
        String original = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) ext = original.substring(dot);
        String stored = Instant.now().toEpochMilli() + "_" + UUID.randomUUID().toString().replace("-", "") + ext;
        Path dest = root.resolve(stored);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        return stored;
    }

    @Override
    public Resource loadAsResource(String storedFilename) {
        Path path = root.resolve(storedFilename);
        return new FileSystemResource(path);
    }

    @Override
    public Path getStorageRoot() {
        return root;
    }

    @Override
    public void delete(String storedFilename) throws IOException {
        Files.deleteIfExists(root.resolve(storedFilename));
    }
}

