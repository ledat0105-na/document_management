package com.example.document_management.service;

import java.io.File;
import java.util.List;

public interface IExportService {
    File exportZip(List<Long> documentIds);
    File exportAll();
}

