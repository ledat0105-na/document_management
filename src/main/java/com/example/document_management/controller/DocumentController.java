package com.example.document_management.controller;

import com.example.document_management.model.DocumentFile;
import com.example.document_management.service.impl.DocumentServiceImpl;
import com.example.document_management.service.IExportService;
import com.example.document_management.service.IFileStorageService;
import com.example.document_management.service.ICategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentServiceImpl docService;
    private final IFileStorageService storageService;
    private final IExportService exportService;
    private final ICategoryService categoryService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(required = false) String q,
                       @RequestParam(required = false, name = "category") String category,
                       @RequestParam(required = false, name = "showFolders", defaultValue = "false") boolean showFolders,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false, name = "size") Integer size,
                       @RequestParam(required = false, name = "pageSize") Integer pageSize,
                       @RequestParam(defaultValue = "createdAt") String sort,
                       @RequestParam(defaultValue = "desc") String dir,
                       @RequestParam(required = false) String folderSort,
                       @RequestParam(required = false) String folderDir) {
        
        System.out.println("DEBUG: Received pageSize=" + pageSize + ", showFolders=" + showFolders + ", page=" + page + ", size=" + size);

        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        
        if (showFolders) {
            Map<String, Long> categoryToCount = categoryService.categoryCounts();
            List<String> allFolders = new ArrayList<>(categoryService.listAllCategories());
            System.out.println("DEBUG folders (union) = " + allFolders);
            System.out.println("DEBUG counts = " + categoryToCount);
            
            String sortBy = folderSort != null ? folderSort : "name";
            String safeDir = (folderDir != null && (folderDir.equalsIgnoreCase("asc") || folderDir.equalsIgnoreCase("desc"))) ? folderDir : "";
            String safeQ = (q != null && !q.equalsIgnoreCase("null") && !q.trim().isEmpty()) ? q.trim() : null;
            boolean isDesc = "desc".equalsIgnoreCase(safeDir);
            
            if ("name".equals(sortBy)) {
                allFolders.sort(String.CASE_INSENSITIVE_ORDER);
                if (isDesc) {
                    Collections.reverse(allFolders);
                }
            } else if ("count".equals(sortBy)) {
                allFolders.sort((a, b) -> {
                    long countA = categoryToCount.getOrDefault(a, 0L);
                    long countB = categoryToCount.getOrDefault(b, 0L);
                    int result = Long.compare(countA, countB);
                    return isDesc ? -result : result;
                });
            } else if ("newest".equals(sortBy)) {
                allFolders.sort((a, b) -> {
                    int result = Integer.compare(a.length(), b.length());
                    if (result == 0) {
                        result = a.compareToIgnoreCase(b);
                    }
                    return isDesc ? -result : result;
                });
            }
            
            if (safeQ != null && !safeQ.isEmpty()) {
                String searchTerm = safeQ.toLowerCase();
                allFolders = allFolders.stream()
                        .filter(folder -> folder.toLowerCase().contains(searchTerm))
                        .collect(Collectors.toList());
            }

            int folderPageSize = (pageSize != null && pageSize > 0) ? pageSize : (size != null && size > 0) ? size : 9;
            int from = Math.min(page * folderPageSize, allFolders.size());
            int to = Math.min(from + folderPageSize, allFolders.size());
            List<String> paginatedFolders = from < to ? allFolders.subList(from, to) : List.of();
            
            System.out.println("DEBUG: folderPageSize=" + folderPageSize + ", page=" + page + ", from=" + from + ", to=" + to);
            System.out.println("DEBUG: allFolders.size()=" + allFolders.size() + ", paginatedFolders.size()=" + paginatedFolders.size());
            System.out.println("DEBUG: pageSize=" + pageSize + ", size=" + size + ", final folderPageSize=" + folderPageSize);

            int totalFolders = allFolders.size();
            int totalPages = (int) Math.ceil((double) totalFolders / folderPageSize);
            if (totalPages == 0) totalPages = 1;
            if (page >= totalPages) {
                page = Math.max(0, totalPages - 1);
                from = Math.min(page * folderPageSize, allFolders.size());
                to = Math.min(from + folderPageSize, allFolders.size());
                paginatedFolders = from < to ? allFolders.subList(from, to) : List.of();
            }

            Page<com.example.document_management.model.dto.FolderDto> folderPage = new PageImpl<>(
                    paginatedFolders.stream()
                            .map(n -> new com.example.document_management.model.dto.FolderDto(n, categoryToCount.getOrDefault(n, 0L)))
                            .toList(),
                    PageRequest.of(page, folderPageSize),
                    allFolders.size());

            model.addAttribute("folderPage", folderPage);
            model.addAttribute("showFolders", true);
            model.addAttribute("folderSort", sortBy);
            model.addAttribute("pageSize", pageSize);
            model.addAttribute("size", size);
            model.addAttribute("folderDir", safeDir);
            model.addAttribute("q", safeQ);
            model.addAttribute("docPage", new PageImpl<DocumentFile>(List.of(), PageRequest.of(0, 9), 0));
        } else if (category != null && category.equals("archived")) {
            int actualPageSize = (pageSize != null) ? pageSize : 9;
            Pageable pageable = PageRequest.of(page, actualPageSize, Sort.by(direction, sort));
            Page<DocumentFile> docPage = docService.list(q, pageable, true);
            Page<com.example.document_management.model.dto.DocumentDto> docDtoPage = docPage.map(com.example.document_management.model.mapper.DocumentMapper::toDto);
            model.addAttribute("docPage", docDtoPage);
            model.addAttribute("q", q);
            model.addAttribute("category", category);
            model.addAttribute("sort", sort);
            model.addAttribute("dir", dir);
            model.addAttribute("pageSize", actualPageSize);
            model.addAttribute("showFolders", false);
            model.addAttribute("folderPage", new PageImpl<String>(List.of(), PageRequest.of(0, 9), 0));
        } else {
            int actualPageSize = (pageSize != null) ? pageSize : 9;
            Pageable pageable = PageRequest.of(page, actualPageSize, Sort.by(direction, sort));
            Page<DocumentFile> docPage;
            
            if (category != null && !category.isBlank() && !category.equals("archived")) {
                List<DocumentFile> all = docService
                        .list(q, PageRequest.of(0, Integer.MAX_VALUE, Sort.by(direction, sort)))
                        .getContent();
                List<DocumentFile> filtered = all.stream()
                        .filter(d -> (d.getCategory() != null && d.getCategory().equalsIgnoreCase(category)))
                        .filter(d -> !d.isArchived())
                        .toList();
                int from = Math.min(page * actualPageSize, filtered.size());
                int to = Math.min(from + actualPageSize, filtered.size());
                List<DocumentFile> content = from < to ? filtered.subList(from, to) : List.of();
                docPage = new PageImpl<>(content, PageRequest.of(page, actualPageSize, Sort.by(direction, sort)), filtered.size());
            } else {
                List<DocumentFile> all = docService
                        .list(q, PageRequest.of(0, Integer.MAX_VALUE, Sort.by(direction, sort)))
                        .getContent();
                List<DocumentFile> filtered = all.stream()
                        .filter(d -> !d.isArchived())
                        .toList();
                int total = filtered.size();
                int totalPages = (int) Math.ceil((double) total / actualPageSize);
                if (totalPages == 0) totalPages = 1;
                if (page >= totalPages) {
                    page = Math.max(0, totalPages - 1);
                }
                int from = Math.min(page * actualPageSize, filtered.size());
                int to = Math.min(from + actualPageSize, filtered.size());
                List<DocumentFile> content = from < to ? filtered.subList(from, to) : List.of();
                docPage = new PageImpl<>(content, PageRequest.of(page, actualPageSize, Sort.by(direction, sort)), filtered.size());
            }

            Page<com.example.document_management.model.dto.DocumentDto> docDtoPage = docPage.map(com.example.document_management.model.mapper.DocumentMapper::toDto);
            model.addAttribute("docPage", docDtoPage);
        model.addAttribute("q", q);
            model.addAttribute("category", category);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("pageSize", actualPageSize);
            model.addAttribute("showFolders", false);
            model.addAttribute("folderPage", new PageImpl<String>(List.of(), PageRequest.of(0, 9), 0));
        }

        List<DocumentFile> allForCats = docService
                .list(null, PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "title")))
                .getContent();
        List<String> categories = allForCats.stream()
                .map(DocumentFile::getCategory)
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        
        // Use categoryService to include all pre-created folders
        List<String> allCategories = categoryService.listAllCategories();
        
        model.addAttribute("categories", allCategories);
        model.addAttribute("allCategories", allCategories);
        long totalDocs = docService.countTotalDocuments(false);
        long archivedDocs = docService.countTotalDocuments(true);
        model.addAttribute("totalDocs", totalDocs);
        model.addAttribute("archivedDocs", archivedDocs);
        try {
            int folderTotal = categoryService.listAllCategories().size();
            model.addAttribute("folderTotal", folderTotal);
        } catch (Exception ignored) {}
        return "documents/list_simple";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("doc", new DocumentFile());
        // Load categories from category service to include pre-created folders
        List<String> categories = categoryService.listAllCategories();
        model.addAttribute("categories", categories);
        long totalDocs = docService.countTotalDocuments(false);
        long archivedDocs = docService.countTotalDocuments(true);
        model.addAttribute("totalDocs", totalDocs);
        model.addAttribute("archivedDocs", archivedDocs);
        try {
            int folderTotal = categoryService.listAllCategories().size();
            model.addAttribute("folderTotal", folderTotal);
        } catch (Exception ignored) {}
        return "documents/form_simple";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("doc") DocumentFile doc,
                         BindingResult result,
                         @RequestParam("file") MultipartFile file,
                         @RequestParam(value = "categorySelect", required = false) String categorySelect,
                         @RequestParam(value = "categoryNew", required = false) String categoryNew,
                         @RequestParam(value = "returnPage", defaultValue = "0") int returnPage,
                         @RequestParam(value = "returnCategory", required = false) String returnCategory,
                         @RequestParam(value = "returnQ", required = false) String returnQ,
                         Model model,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("message", "Có lỗi trong thông tin nhập vào");
            ra.addFlashAttribute("messageType", "error");
            return "redirect:/documents";
        }
        if (file == null || file.isEmpty()) {
            ra.addFlashAttribute("message", "Vui lòng chọn tệp tải lên");
            ra.addFlashAttribute("messageType", "error");
            return "redirect:/documents";
        }
        try {
            String cat = (categoryNew != null && !categoryNew.isBlank()) ? categoryNew.trim() : 
                        (categorySelect != null && !categorySelect.isBlank()) ? categorySelect.trim() : null;
            if (cat != null && !cat.isBlank()) doc.setCategory(cat);
            String stored = storageService.store(file);
            docService.create(doc, file.getOriginalFilename(), stored);
            ra.addFlashAttribute("message", "Đã thêm tài liệu '" + doc.getTitle() + "' thành công");
            ra.addFlashAttribute("messageType", "success");
            
            StringBuilder redirectUrl = new StringBuilder("/documents?page=").append(returnPage);
            if (returnCategory != null && !returnCategory.isBlank()) {
                redirectUrl.append("&category=").append(returnCategory);
            }
            if (returnQ != null && !returnQ.isBlank()) {
                redirectUrl.append("&q=").append(returnQ);
            }
            return "redirect:" + redirectUrl.toString();
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("message", e.getMessage());
            ra.addFlashAttribute("messageType", "error");
            return "redirect:/documents";
        } catch (Exception e) {
            ra.addFlashAttribute("message", "Lỗi khi tải lên: " + e.getMessage());
            ra.addFlashAttribute("messageType", "error");
            return "redirect:/documents";
        }
    }

    
    @GetMapping("/{id}/details")
    @ResponseBody
    public Map<String, Object> getDocumentDetails(@PathVariable Long id) {
        DocumentFile doc = docService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài liệu"));
        
        Map<String, Object> details = new HashMap<>();
        details.put("id", doc.getId());
        details.put("title", doc.getTitle());
        details.put("description", doc.getDescription());
        details.put("category", doc.getCategory());
        details.put("originalFilename", doc.getOriginalFilename());
        details.put("archived", doc.isArchived());
        details.put("createdAt", doc.getCreatedAt() != null ? 
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(
                java.time.LocalDateTime.ofInstant(doc.getCreatedAt(), java.time.ZoneId.systemDefault())
            ) : null);
        details.put("updatedAt", doc.getUpdatedAt() != null ? 
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(
                java.time.LocalDateTime.ofInstant(doc.getUpdatedAt(), java.time.ZoneId.systemDefault())
            ) : null);
        
        return details;
    }

    @GetMapping("/categories")
    @ResponseBody
    public List<String> getAllCategories() {
        return docService.list(null, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent().stream()
                .map(DocumentFile::getCategory)
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        DocumentFile d = docService.findById(id).orElse(null);
        if (d == null) return "redirect:/documents";
        model.addAttribute("doc", d);
        List<String> categories = docService
                .list(null, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent().stream()
                .map(DocumentFile::getCategory)
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        model.addAttribute("categories", categories);
        return "documents/edit_simple";
    }

    @PostMapping("/{id}")
    public Object update(@PathVariable Long id,
                         @Valid @ModelAttribute("doc") DocumentFile form,
                         BindingResult result,
                         @RequestParam(value = "categorySelect", required = false) String categorySelect,
                         @RequestParam(value = "categoryNew", required = false) String categoryNew,
                         @RequestParam(value = "file", required = false) MultipartFile newFile,
                         HttpServletRequest request) {
        
        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        
        if (result.hasErrors()) {
            if (isAjax) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Có lỗi trong thông tin nhập vào");
                return ResponseEntity.badRequest().body(response);
            }
            return "documents/detail";
        }
        
        String cat = (categoryNew != null && !categoryNew.isBlank()) ? categoryNew.trim() : 
                    (categorySelect != null && !categorySelect.isBlank()) ? categorySelect.trim() : null;
        String newStored = null;
        String newOriginal = null;
        try {
            if (newFile != null && !newFile.isEmpty()) {
                newStored = storageService.store(newFile);
                newOriginal = newFile.getOriginalFilename();
            }
        } catch (Exception e) {
            if (isAjax) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Lỗi khi tải lên file: " + e.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            result.reject("uploadError", e.getMessage());
            return "documents/detail";
        }

        try {
            docService.update(id, form.getTitle(), form.getDescription(), cat,
                    newOriginal, newStored);
            
            if (isAjax) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Đã cập nhật tài liệu thành công");
                return ResponseEntity.ok(response);
            }
            
            return "redirect:/documents";
        } catch (IllegalArgumentException e) {
            if (isAjax) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", e.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            result.reject("titleExists", e.getMessage());
            return "documents/detail";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        docService.findById(id).ifPresent(d -> {
            try { storageService.delete(d.getStoredFilename()); } catch (Exception ignored) {}
        });
        docService.delete(id);
        redirectAttributes.addFlashAttribute("message", "Xóa tệp thành công");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/documents";
    }

    @PostMapping("/bulk-delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> bulkDelete(@RequestParam("documentIds") List<Long> documentIds) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int deletedCount = 0;
            for (Long id : documentIds) {
                docService.findById(id).ifPresent(d -> {
                    try { storageService.delete(d.getStoredFilename()); } catch (Exception ignored) {}
                });
                docService.delete(id);
                deletedCount++;
            }
            
            response.put("success", true);
            response.put("message", "Đã xóa " + deletedCount + " tài liệu thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi xóa tài liệu: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/{id}/archive")
    public String archive(@PathVariable Long id) {
        docService.setArchived(id, true);
        return "redirect:/documents?category=archived";
    }
    
    @PostMapping("/{id}/unarchive")
    public String unarchive(@PathVariable Long id) {
        docService.setArchived(id, false);
        return "redirect:/documents";
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        DocumentFile d = docService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài liệu"));
        Resource res = storageService.loadAsResource(d.getStoredFilename());
        if (res == null || !res.exists()) {
            return ResponseEntity.notFound().build();
        }
        String filename = d.getOriginalFilename();
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            Path p = storageService.getStorageRoot().resolve(d.getStoredFilename());
            String detected = Files.probeContentType(p);
            if (detected != null) {
                mediaType = MediaType.parseMediaType(detected);
            }
        } catch (Exception ignored) {}
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .body(res);
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> viewInline(@PathVariable Long id) {
        DocumentFile d = docService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài liệu"));
        Resource res = storageService.loadAsResource(d.getStoredFilename());
        if (res == null || !res.exists()) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            Path p = storageService.getStorageRoot().resolve(d.getStoredFilename());
            String detected = Files.probeContentType(p);
            if (detected != null) {
                mediaType = MediaType.parseMediaType(detected);
            }
        } catch (Exception ignored) {}
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(res);
    }

    @GetMapping("/{id}/preview")
    public String previewPage(@PathVariable Long id, 
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "category", required = false) String category,
                             @RequestParam(value = "q", required = false) String q,
                             Model model) {
        DocumentFile d = docService.findById(id).orElse(null);
        if (d == null) return "redirect:/documents";
        model.addAttribute("doc", d);
        
        model.addAttribute("backPage", page);
        model.addAttribute("backCategory", category);
        model.addAttribute("backQ", q);
        
        return "documents/preview";
    }

    @GetMapping("/export/all")
    public ResponseEntity<Resource> exportAll() {
        File zip = exportService.exportAll();
        return fileResponse(zip, "documents_export.zip");
    }

    @PostMapping("/export")
    public ResponseEntity<Resource> exportSelected(@RequestParam("ids") List<Long> ids) {
        File zip = exportService.exportZip(ids);
        return fileResponse(zip, "documents_selected.zip");
    }

    private ResponseEntity<Resource> fileResponse(File file, String downloadName) {
        Resource res = org.springframework.core.io.FileSystemResourceLoader
                .class.cast(new org.springframework.core.io.FileSystemResourceLoader())
                .getResource(file.getAbsolutePath());
        String fname = URLEncoder.encode(downloadName, StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fname)
                .body(res);
    }

    @GetMapping("/folders")
    public String folders(Model model) {
        return "redirect:/documents?showFolders=true";
    }

    @GetMapping("/folders/{name}")
    public String folderDetail(@PathVariable("name") String name,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "9") int size,
                               Model model) {
        var all = docService.list(null, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        var inFolder = all.stream()
                .filter(d -> d.getCategory() != null && d.getCategory().equalsIgnoreCase(name))
                .toList();
        
        int from = Math.min(page * size, inFolder.size());
        int to = Math.min(from + size, inFolder.size());
        var content = from < to ? inFolder.subList(from, to) : java.util.List.of();
        
        Pageable pageable = PageRequest.of(page, size);
        Page<DocumentFile> folderPage = new PageImpl<DocumentFile>((List<DocumentFile>) content, pageable, inFolder.size());
        
        List<String> allCategories = docService
                .list(null, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent().stream()
                .map(DocumentFile::getCategory)
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        
        model.addAttribute("docPage", folderPage);
        model.addAttribute("folderName", name);
        model.addAttribute("allCategories", allCategories);
        model.addAttribute("currentCategory", name);
        return "documents/folder_detail";
    }

    @PostMapping("/folders/{name}/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteFolder(@PathVariable("name") String name) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean ok = categoryService.deleteCategory(name);
            if (ok) {
                response.put("success", true);
                response.put("message", "Đã xóa thư mục '" + name + "' thành công");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Không thể xóa thư mục vì vẫn còn file bên trong");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi xóa thư mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/folders/{name}/rename")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> renameFolderPost(@PathVariable("name") String oldName,
                                                               @RequestParam("newName") String newName) {
        try {
            oldName = java.net.URLDecoder.decode(oldName, "UTF-8");
        } catch (Exception e) {
        }
        Map<String, Object> response = new HashMap<>();
        
        if (newName == null || newName.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Tên mới không được để trống");
            return ResponseEntity.badRequest().body(response);
        }
        
        String trimmedNewName = newName.trim();
        if (trimmedNewName.equals(oldName)) {
            response.put("success", false);
            response.put("message", "Tên mới phải khác tên hiện tại");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            System.out.println("DEBUG: Attempting to rename folder from '" + oldName + "' to '" + trimmedNewName + "'");
            boolean ok = categoryService.renameCategory(oldName, trimmedNewName);
            System.out.println("DEBUG: Rename result: " + ok);
            if (ok) {
                response.put("success", true);
                response.put("message", "Đã đổi tên thư mục từ '" + oldName + "' thành '" + trimmedNewName + "'");
                System.out.println("DEBUG: Returning success response");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Tên thư mục '" + trimmedNewName + "' đã tồn tại. Vui lòng chọn tên khác.");
                System.out.println("DEBUG: Returning duplicate name error");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Exception during rename: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi khi đổi tên thư mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/folders/bulk-delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> bulkDeleteFolders(@RequestParam("folderNames") List<String> folderNames) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int deletedCount = 0;
            int totalCount = folderNames.size();
            
            for (String folderName : folderNames) {
                boolean ok = categoryService.deleteCategory(folderName);
                if (ok) {
                    deletedCount++;
                }
            }
            
            if (deletedCount > 0) {
                response.put("success", true);
                response.put("message", "Đã xóa " + deletedCount + "/" + totalCount + " thư mục thành công");
                response.put("deletedCount", deletedCount);
                response.put("totalCount", totalCount);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Không thể xóa thư mục nào");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi xóa thư mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/folders/bulk-move")
    public String bulkMoveFolders(@RequestParam("folderNames") List<String> folderNames,
                                  @RequestParam("targetCategory") String targetCategory,
                                  RedirectAttributes ra) {
        try {
            int movedCount = 0;
            for (String folderName : folderNames) {
                boolean ok = categoryService.renameCategory(folderName, targetCategory);
                if (ok) {
                    movedCount++;
                }
            }
            
            if (movedCount > 0) {
                ra.addFlashAttribute("message", "Đã chuyển " + movedCount + " thư mục sang '" + targetCategory + "'");
            } else {
                ra.addFlashAttribute("error", "Không thể chuyển thư mục nào");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi chuyển thư mục: " + e.getMessage());
        }
        
        return "redirect:/documents?showFolders=true";
    }

    @PostMapping("/documents/{id}/move")
    public String moveDocumentToFolder(@PathVariable("id") Long documentId,
                                       @RequestParam("newCategory") String newCategory,
                                       RedirectAttributes ra) {
        try {
            DocumentFile doc = docService.findById(documentId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài liệu"));
            
            String oldCategory = doc.getCategory();
            String trimmedCategory = (newCategory != null && !newCategory.isBlank()) ? newCategory.trim() : null;
            docService.update(documentId, doc.getTitle(), doc.getDescription(), trimmedCategory, 
                            doc.getOriginalFilename(), doc.getStoredFilename());
            
            ra.addFlashAttribute("message", "Đã chuyển tài liệu '" + doc.getTitle() + 
                                          "' từ '" + oldCategory + "' sang '" + newCategory + "'");
            
            if (oldCategory != null && !oldCategory.trim().isEmpty()) {
                return "redirect:/documents?showFolders=true";
            }
            return "redirect:/documents";
            
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể chuyển tài liệu: " + e.getMessage());
            return "redirect:/documents";
        }
    }

    @PostMapping("/bulk-move")
    public String bulkMoveDocuments(@RequestParam("documentIds") List<Long> ids,
                                    @RequestParam("targetCategory") String targetCategory,
                                    @RequestParam(value = "returnCategory", required = false) String returnCategory,
                                    @RequestParam(value = "returnPage", required = false) Integer returnPage,
                                    RedirectAttributes ra) {
        try {
            int moved = 0;
            for (Long id : ids) {
                DocumentFile doc = docService.findById(id).orElse(null);
                if (doc != null) {
                    docService.update(id, doc.getTitle(), doc.getDescription(), targetCategory, doc.getOriginalFilename(), doc.getStoredFilename());
                    moved++;
                }
            }
            if (moved > 0) {
                ra.addFlashAttribute("message", "Đã chuyển " + moved + " tài liệu");
            } else {
                ra.addFlashAttribute("error", "Không có tài liệu nào được chuyển");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi chuyển tài liệu: " + e.getMessage());
        }
        return "redirect:/documents?showFolders=true";
    }

    @PostMapping("/folders")
    public String addFolder(@RequestParam("folderName") String folderName, RedirectAttributes ra) {
        if (folderName != null && !folderName.trim().isEmpty()) {
            categoryService.ensureCategoryExists(folderName.trim());
            ra.addFlashAttribute("message", "Đã tạo thư mục '" + folderName + "'");
            int total = categoryService.listAllCategories().size();
            int pageSize = 9;
            int lastPage = Math.max(0, (total - 1) / pageSize);
            return "redirect:/documents?showFolders=true&page=" + lastPage;
        } else {
            ra.addFlashAttribute("error", "Tên thư mục không được để trống");
        }
        return "redirect:/documents?showFolders=true";
    }

    @GetMapping("/folders/{name}/rename")
    public String renameFolder(@PathVariable("name") String oldName, 
                              @RequestParam("newName") String newName, 
                              RedirectAttributes ra) {
        if (newName != null && !newName.trim().isEmpty()) {
            boolean ok = categoryService.renameCategory(oldName, newName);
            if (ok) {
                ra.addFlashAttribute("message", "Đã đổi tên thư mục từ '" + oldName + "' thành '" + newName + "'");
            } else {
                ra.addFlashAttribute("error", "Tên thư mục '" + newName + "' đã tồn tại. Vui lòng chọn tên khác.");
            }
        } else {
            ra.addFlashAttribute("error", "Tên mới không được để trống");
        }
        return "redirect:/documents?showFolders=true";
    }

}


