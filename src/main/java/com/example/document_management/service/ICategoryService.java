package com.example.document_management.service;

import java.util.List;
import java.util.Map;

public interface ICategoryService {
    Map<String, Long> categoryCounts();
    List<String> listAllCategories();
    void ensureCategoryExists(String name);
    boolean deleteCategory(String name);
    boolean renameCategory(String oldName, String newName);
}



