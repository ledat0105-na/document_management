package com.example.document_management.service.impl;

import com.example.document_management.model.Category;
import com.example.document_management.model.DocumentFile;
import com.example.document_management.repository.ICategoryRepository;
import com.example.document_management.repository.IDocumentRepository;
import com.example.document_management.service.IFileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl {

    private final IDocumentRepository repo;
    private final IFileStorageService storage;
    private final ICategoryRepository categoryRepo;

    public Page<DocumentFile> list(String q, Pageable pageable) {
        Page<DocumentFile> page = repo.findAll(pageable);
        if (q == null || q.isBlank()) return page;
        List<DocumentFile> filtered = page.getContent().stream()
                .filter(d -> (d.getTitle() != null && d.getTitle().toLowerCase().contains(q.toLowerCase()))
                        || (d.getDescription() != null && d.getDescription().toLowerCase().contains(q.toLowerCase())))
                .toList();
        return new PageImpl<>(filtered, pageable, filtered.size());
    }

    public Page<DocumentFile> list(String q, Pageable pageable, boolean archived) {
        
        List<DocumentFile> sortedAll = repo.findAll(pageable.getSort());
        List<DocumentFile> filtered = sortedAll.stream()
                .filter(d -> d.isArchived() == archived)
                .filter(d -> q == null || q.isBlank() ||
                        (d.getTitle() != null && d.getTitle().toLowerCase().contains(q.toLowerCase()))
                        || (d.getDescription() != null && d.getDescription().toLowerCase().contains(q.toLowerCase())))
                .toList();
        int from = Math.min((int) pageable.getOffset(), filtered.size());
        int to = Math.min(from + pageable.getPageSize(), filtered.size());
        List<DocumentFile> content = from < to ? filtered.subList(from, to) : List.of();
        return new PageImpl<>(content, pageable, filtered.size());
    }

    public long countTotalDocuments(boolean archived) {
        return repo.findAll().stream()
                .filter(d -> d.isArchived() == archived)
                .count();
    }

    public Optional<DocumentFile> findById(Long id) { return repo.findById(id); }
    
    public boolean isTitleExists(String title) {
        if (title == null || title.isBlank()) return false;
        return repo.findAll().stream()
                .anyMatch(doc -> title.equalsIgnoreCase(doc.getTitle()));
    }

    public DocumentFile create(DocumentFile meta, String originalFilename, String storedFilename) {
        
        if (isTitleExists(meta.getTitle())) {
            throw new IllegalArgumentException("Tài liệu với tiêu đề '" + meta.getTitle() + "' đã tồn tại. Vui lòng chọn tiêu đề khác.");
        }
        
        meta.setOriginalFilename(originalFilename);
        meta.setStoredFilename(storedFilename);
        
        Instant now = getCurrentTimestamp();
        meta.setCreatedAt(now);
        meta.setUpdatedAt(now);
        meta.setArchived(false);
        if (meta.getCategory() != null && !meta.getCategory().isBlank()) {
            ensureCategoryExists(meta.getCategory());
        }
        return repo.save(meta);
    }

    public DocumentFile update(Long id, String title, String description, String category,
                               String newOriginalFilename, String newStoredFilename) {
        DocumentFile d = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài liệu"));
        
        
        if (!title.equals(d.getTitle()) && isTitleExists(title)) {
            throw new IllegalArgumentException("Tài liệu với tiêu đề '" + title + "' đã tồn tại. Vui lòng chọn tiêu đề khác.");
        }
        
        d.setTitle(title);
        d.setDescription(description);
        if (category != null && !category.isBlank()) {
            d.setCategory(category.trim());
            ensureCategoryExists(category.trim());
        } else {
            d.setCategory(null); 
        }
        if (newStoredFilename != null && newOriginalFilename != null) {
            d.setStoredFilename(newStoredFilename);
            d.setOriginalFilename(newOriginalFilename);
        }
        d.setUpdatedAt(Instant.now());
        return repo.save(d);
    }

    public void delete(Long id) {
        repo.findById(id).ifPresent(d -> {
            try { storage.delete(d.getStoredFilename()); } catch (Exception ignored) {}
            repo.delete(d);
        });
    }

    public DocumentFile setArchived(Long id, boolean archived) {
        DocumentFile d = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài liệu"));
        d.setArchived(archived);
        d.setUpdatedAt(Instant.now());
        return repo.save(d);
    }

    public boolean renameCategory(String oldName, String newName) {
        if (oldName == null || newName == null || oldName.isBlank() || newName.isBlank()) return false;
        List<DocumentFile> list = repo.findAll().stream().peek(d -> {
            if (d.getCategory() != null && oldName.equalsIgnoreCase(d.getCategory())) {
                d.setCategory(newName.trim());
            }
        }).toList();
        repo.saveAll(list);
        
        if (!categoryRepo.existsById(newName.trim())) {
            categoryRepo.save(new Category(newName.trim()));
        }
        return true;
    }

    public Map<String, Long> categoryCounts() {
        Map<String, Long> counts = repo.findAll().stream()
                .filter(d -> d.getCategory() != null && !d.getCategory().isBlank())
                .collect(Collectors.groupingBy(d -> d.getCategory().trim(), Collectors.counting()));
        
        categoryRepo.findAll().forEach(c -> counts.putIfAbsent(c.getName(), 0L));
        return counts;
    }

    public List<String> listAllCategories() {
        
        Set<String> allCategories = new HashSet<>();
        
        
        categoryRepo.findAll().forEach(c -> allCategories.add(c.getName()));
        
        
        repo.findAll().stream()
                .map(DocumentFile::getCategory)
                .filter(cat -> cat != null && !cat.isBlank())
                .map(String::trim)
                .forEach(allCategories::add);
        
        return allCategories.stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    
    public void syncCategoriesFromDocuments() {
        repo.findAll().stream()
                .map(DocumentFile::getCategory)
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .distinct()
                .forEach(this::ensureCategoryExists);
    }

    public boolean deleteCategory(String name) {
        if (name == null || name.isBlank()) return false;
        long cnt = repo.findAll().stream()
            .filter(d -> d.getCategory() != null && name.equalsIgnoreCase(d.getCategory()))
            .count();
        if (cnt == 0) {
            
            categoryRepo.findById(name).ifPresent(categoryRepo::delete);
            return true;
        }
        return false;
    }

    public void ensureCategoryExists(String name) {
        if (name == null) return;
        String n = name.trim();
        if (n.isEmpty()) return;
        if (!categoryRepo.existsById(n)) {
            categoryRepo.save(new Category(n));
        }
    }
    
    
    private Instant getCurrentTimestamp() {
        return Instant.now();
    }
}


