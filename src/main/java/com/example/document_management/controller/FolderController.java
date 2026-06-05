package com.example.document_management.controller;

import com.example.document_management.model.dto.FolderDto;
import com.example.document_management.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/folders")
public class FolderController {

    private final ICategoryService categoryService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String sort,
                       @RequestParam(required = false) String dir) {

        Map<String, Long> counts = categoryService.categoryCounts();
        List<String> all = new ArrayList<>(categoryService.listAllCategories());

        String sortBy = sort != null ? sort : "name";
        boolean isDesc = "desc".equalsIgnoreCase(dir);
        if ("name".equals(sortBy)) {
            all.sort(String.CASE_INSENSITIVE_ORDER);
            if (isDesc) Collections.reverse(all);
        } else if ("count".equals(sortBy)) {
            all.sort((a,b) -> {
                long ca = counts.getOrDefault(a,0L);
                long cb = counts.getOrDefault(b,0L);
                int r = Long.compare(ca, cb);
                return isDesc ? -r : r;
            });
        }

        if (q != null && !q.isBlank()) {
            String s = q.trim().toLowerCase();
            all = all.stream().filter(n -> n.toLowerCase().contains(s)).collect(Collectors.toList());
        }

        int pageSize = 9;
        int totalPages = (int) Math.ceil((double) all.size() / pageSize);
        if (totalPages == 0) totalPages = 1;
        if (page >= totalPages) page = Math.max(0, totalPages - 1);

        int from = Math.min(page * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        List<String> items = from < to ? all.subList(from, to) : List.of();

        Page<FolderDto> folderPage = new PageImpl<>(
                items.stream().map(n -> new FolderDto(n, counts.getOrDefault(n, 0L))).toList(),
                PageRequest.of(page, pageSize),
                all.size()
        );

        model.addAttribute("folderPage", folderPage);
        model.addAttribute("q", q);
        model.addAttribute("sort", sortBy);
        model.addAttribute("dir", dir);
        return "documents/list_simple"; 
    }
}



