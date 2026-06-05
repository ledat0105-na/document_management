package com.example.document_management.service.impl;

import com.example.document_management.model.Category;
import com.example.document_management.model.DocumentFile;
import com.example.document_management.repository.ICategoryRepository;
import com.example.document_management.repository.IDocumentRepository;
import com.example.document_management.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    private final ICategoryRepository categoryRepo;
    private final IDocumentRepository documentRepo;

    @Override
    public Map<String, Long> categoryCounts() {
        Map<String, Long> counts = documentRepo.findAll().stream()
                .filter(d -> d.getCategory() != null && !d.getCategory().isBlank())
                .collect(Collectors.groupingBy(d -> d.getCategory().trim(), Collectors.counting()));
        categoryRepo.findAll().forEach(c -> counts.putIfAbsent(c.getName(), 0L));
        return counts;
    }

    @Override
    public List<String> listAllCategories() {

        Set<String> allCategories = new HashSet<>();
        

        categoryRepo.findAll().forEach(c -> allCategories.add(c.getName()));
        

        documentRepo.findAll().stream()
                .filter(d -> d.getCategory() != null && !d.getCategory().isBlank())
                .forEach(d -> allCategories.add(d.getCategory().trim()));
        
        return allCategories.stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    @Override
    public void ensureCategoryExists(String name) {
        if (name == null) return;
        String n = name.trim();
        if (n.isEmpty()) return;
        if (!categoryRepo.existsById(n)) {
            categoryRepo.save(new Category(n));
        }
    }

    @Override
    public boolean deleteCategory(String name) {
        if (name == null || name.isBlank()) return false;
        long cnt = documentRepo.findAll().stream()
            .filter(d -> d.getCategory() != null && name.equalsIgnoreCase(d.getCategory()))
            .count();
        if (cnt == 0) {
            categoryRepo.findById(name).ifPresent(categoryRepo::delete);
            return true;
        }
        return false;
    }

    @Override
    public boolean renameCategory(String oldName, String newName) {
        System.out.println("DEBUG: renameCategory called with oldName='" + oldName + "', newName='" + newName + "'");
        
        if (oldName == null || newName == null || oldName.isBlank() || newName.isBlank()) {
            System.out.println("DEBUG: Invalid input parameters");
            return false;
        }
        
        if (oldName.equalsIgnoreCase(newName.trim())) {
            System.out.println("DEBUG: Same name, no change needed");
            return false; 
        }
        

        if (categoryRepo.existsById(newName.trim())) {
            System.out.println("DEBUG: New name already exists in category table");
            return false; 
        }
        

        boolean newNameExistsInDocs = documentRepo.findAll().stream()
            .anyMatch(d -> d.getCategory() != null && newName.trim().equalsIgnoreCase(d.getCategory()));
        
        if (newNameExistsInDocs) {
            System.out.println("DEBUG: New name already exists in documents");
            return false; 
        }
        

        List<DocumentFile> documentsToUpdate = documentRepo.findAll().stream()
            .filter(d -> d.getCategory() != null && oldName.equalsIgnoreCase(d.getCategory()))
            .peek(d -> d.setCategory(newName.trim()))
            .toList();
        
        System.out.println("DEBUG: Found " + documentsToUpdate.size() + " documents to update");
        

        documentRepo.saveAll(documentsToUpdate);
        

        ensureCategoryExists(newName.trim());
        

        categoryRepo.findById(oldName).ifPresent(categoryRepo::delete);
        
        System.out.println("DEBUG: Rename completed successfully");
        return true;
    }
}



