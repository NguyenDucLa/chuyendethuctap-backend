package com.football.booking_system.controller;

import com.football.booking_system.entity.Category;
import com.football.booking_system.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin("*")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    // 1. Lấy tất cả danh mục (Public - Dùng chung với CourtController cũ cũng được)
    @GetMapping
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    // 2. Thêm danh mục mới (Admin)
    @PostMapping
    public Category create(@RequestBody Category category) {
        return categoryRepository.save(category);
    }

    // 3. Sửa danh mục (Admin)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Category request) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) return ResponseEntity.notFound().build();

        category.setName(request.getName());
        categoryRepository.save(category);
        return ResponseEntity.ok(category);
    }

    // 4. Xóa danh mục (Admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!categoryRepository.existsById(id)) return ResponseEntity.notFound().build();

        try {
            categoryRepository.deleteById(id);
            return ResponseEntity.ok("Xóa thành công");
        } catch (Exception e) {
            // Lỗi này thường do danh mục đang có sân con liên kết (Foreign Key)
            return ResponseEntity.badRequest().body("Không thể xóa danh mục này vì đang có sân thuộc về nó.");
        }
    }
}