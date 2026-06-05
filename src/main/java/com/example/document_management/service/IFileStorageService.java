package com.example.document_management.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface IFileStorageService {
    String store(MultipartFile file) throws IOException;         
    Resource loadAsResource(String storedFilename);
    Path getStorageRoot();
    void delete(String storedFilename) throws IOException;
}


