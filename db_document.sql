-- Database and schema for document management with folders (categories)

CREATE DATABASE IF NOT EXISTS document_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE document_management;

-- 1) Categories (folders) can exist even when empty
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at DATETIME(6) NOT NULL DEFAULT NOW(6),
    updated_at DATETIME(6) NOT NULL DEFAULT NOW(6)
) ENGINE=InnoDB;

-- 2) Documents table (compatible with current application)
CREATE TABLE IF NOT EXISTS document_files (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    category VARCHAR(100), -- folder name (string-based)
    stored_filename VARCHAR(400) NOT NULL UNIQUE,
    original_filename VARCHAR(400) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    archived BIT NOT NULL
) ENGINE=InnoDB;

-- 3) Helper view: category -> counts
DROP VIEW IF EXISTS v_category_counts;
CREATE VIEW v_category_counts AS
SELECT COALESCE(c.name, df.category) AS name,
       COUNT(df.id) AS file_count
FROM categories c
LEFT JOIN document_files df ON df.category = c.name
GROUP BY COALESCE(c.name, df.category);

-- 4) Triggers & procedures
-- Prevent deleting a category if it still has files
DROP TRIGGER IF EXISTS trg_categories_before_delete;
DELIMITER $$
CREATE TRIGGER trg_categories_before_delete
BEFORE DELETE ON categories
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM document_files WHERE category = OLD.name LIMIT 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot delete category with existing files';
    END IF;
END$$
DELIMITER ;

-- Ensure category exists; create if missing
DROP PROCEDURE IF EXISTS sp_ensure_category;
DELIMITER $$
CREATE PROCEDURE sp_ensure_category(IN p_name VARCHAR(100))
BEGIN
    IF p_name IS NULL OR LENGTH(TRIM(p_name)) = 0 THEN LEAVE proc; END IF;
    SET p_name = TRIM(p_name);
    IF NOT EXISTS (SELECT 1 FROM categories WHERE name = p_name) THEN
        INSERT INTO categories(name, created_at, updated_at) VALUES(p_name, NOW(6), NOW(6));
    END IF;
END$$
DELIMITER ;

-- Create category
DROP PROCEDURE IF EXISTS sp_create_category;
DELIMITER $$
CREATE PROCEDURE sp_create_category(IN p_name VARCHAR(100))
BEGIN
    CALL sp_ensure_category(p_name);
END$$
DELIMITER ;

-- Rename category (also update documents)
DROP PROCEDURE IF EXISTS sp_rename_category;
DELIMITER $$
CREATE PROCEDURE sp_rename_category(IN p_old VARCHAR(100), IN p_new VARCHAR(100))
BEGIN
    SET p_old = TRIM(p_old); SET p_new = TRIM(p_new);
    IF p_old IS NULL OR p_new IS NULL OR p_old = '' OR p_new = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid category names';
    END IF;
    START TRANSACTION;
      UPDATE categories SET name = p_new, updated_at = NOW(6) WHERE name = p_old;
      UPDATE document_files SET category = p_new, updated_at = NOW(6) WHERE category = p_old;
    COMMIT;
END$$
DELIMITER ;

-- Delete empty category only
DROP PROCEDURE IF EXISTS sp_delete_category;
DELIMITER $$
CREATE PROCEDURE sp_delete_category(IN p_name VARCHAR(100))
BEGIN
    SET p_name = TRIM(p_name);
    IF EXISTS (SELECT 1 FROM document_files WHERE category = p_name LIMIT 1) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Category not empty';
    END IF;
    DELETE FROM categories WHERE name = p_name;
END$$
DELIMITER ;

-- Move a document into a category (create category if missing)
DROP PROCEDURE IF EXISTS sp_move_document;
DELIMITER $$
CREATE PROCEDURE sp_move_document(IN p_doc_id BIGINT, IN p_target VARCHAR(100))
BEGIN
    SET p_target = TRIM(p_target);
    CALL sp_ensure_category(p_target);
    UPDATE document_files SET category = p_target, updated_at = NOW(6) WHERE id = p_doc_id;
END$$
DELIMITER ;

-- Rename a document title
DROP PROCEDURE IF EXISTS sp_rename_document;
DELIMITER $$
CREATE PROCEDURE sp_rename_document(IN p_doc_id BIGINT, IN p_new_title VARCHAR(255))
BEGIN
    UPDATE document_files SET title = p_new_title, updated_at = NOW(6) WHERE id = p_doc_id;
END$$
DELIMITER ;

-- 5) Seed categories
INSERT IGNORE INTO categories(name) VALUES
    ('Quan trọng'), ('Khác'), ('123'), ('abc'), ('Biểu mẫu'), ('Hợp đồng'), ('Tài chính');

-- 6) Seed documents (metadata only)
INSERT INTO document_files (
    title, description, category, stored_filename, original_filename, created_at, updated_at, archived
) VALUES
    ('Tài liệu mẫu 1', 'Mẫu', 'Quan trọng', 'seed_001_sample1.pdf', 'sample1.pdf', NOW(6), NOW(6), 0),
    ('Hướng dẫn', 'HDSD', 'Khác', 'seed_002_sample2.docx', 'sample2.docx', NOW(6), NOW(6), 0),
    ('File 123', 'test', '123', 'seed_003_file123.pdf', 'file123.pdf', NOW(6), NOW(6), 0),
    ('Ảnh minh họa', 'img', 'abc', 'seed_004_image.jpg', 'image.jpg', NOW(6), NOW(6), 0),
    ('Nền thiết kế', 'bg', 'Khác', 'seed_005_bg.png', 'bg.png', NOW(6), NOW(6), 0),
    ('Biểu mẫu A', 'form', 'Biểu mẫu', 'seed_006_formA.xlsx', 'formA.xlsx', NOW(6), NOW(6), 0),
    ('Hợp đồng B', 'contract', 'Hợp đồng', 'seed_007_contractB.pdf', 'contractB.pdf', NOW(6), NOW(6), 0),
    ('Báo cáo Q4', 'report', 'Tài chính', 'seed_008_reportQ4.xlsx', 'reportQ4.xlsx', NOW(6), NOW(6), 0)
ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at);

-- 7) Quick checks
SELECT id, title, category, archived FROM document_files ORDER BY id;
SELECT name, file_count FROM v_category_counts ORDER BY name;