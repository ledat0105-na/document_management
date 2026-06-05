package com.example.document_management.service.impl;

import com.example.document_management.model.DocumentFile;
import org.springframework.data.domain.PageRequest;
import com.example.document_management.service.IExportService;
import com.example.document_management.service.IFileStorageService;
import com.example.document_management.util.CsvUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements IExportService {

    private final DocumentServiceImpl docService;
    private final IFileStorageService storage;

    @Override
    public File exportZip(List<Long> documentIds) {
        List<DocumentFile> docs = documentIds.stream()
                .map(id -> docService.findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        return makeZip(docs);
    }

    @Override
    public File exportAll() {

        List<DocumentFile> docs = docService.list(null, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        return makeZip(docs);
    }

    private File makeZip(List<DocumentFile> docs) {
        try {
            Path tmp = Files.createTempFile("docs_", ".zip");
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tmp))) {


                String csv = CsvUtils.toCsv(docs);
                zos.putNextEntry(new ZipEntry("metadata.csv"));
                zos.write(csv.getBytes());
                zos.closeEntry();


                for (DocumentFile d : docs) {
                    Path filePath = storage.getStorageRoot().resolve(d.getStoredFilename());
                    if (Files.exists(filePath)) {
                        String sanitized = d.getOriginalFilename() == null ? d.getStoredFilename() : d.getOriginalFilename();

                        String base = sanitized;
                        String entryName = "files/" + base;
                        int counter = 1;
                        java.util.HashSet<String> existing = new java.util.HashSet<>();




                        
                        if (!existing.add(entryName)) {
                            while (!existing.add("files/" + counter + "_" + base)) counter++;
                            entryName = "files/" + counter + "_" + base;
                        }

                        zos.putNextEntry(new ZipEntry(entryName));
                        Files.copy(filePath, zos);
                        zos.closeEntry();
                    }
                }
            }
            return tmp.toFile();
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo file export", e);
        }
    }
}

