package com.example.document_management.repository;

import com.example.document_management.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICategoryRepository extends JpaRepository<Category, String> {
}



