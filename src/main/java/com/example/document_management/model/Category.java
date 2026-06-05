package com.example.document_management.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    public Category() {}

    public Category(String name) { this.name = name; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }
}



