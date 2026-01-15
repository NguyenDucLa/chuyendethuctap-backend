package com.football.booking_system.controller;

import com.football.booking_system.dto.CourtRequest;
import com.football.booking_system.entity.Category;
import com.football.booking_system.entity.Court;
import com.football.booking_system.repository.CategoryRepository;
import com.football.booking_system.service.CourtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courts") // Đường dẫn gốc
@CrossOrigin(origins = "*")    // Cho phép React gọi API
public class CourtController {

    @Autowired
    private CourtService courtService;

    @Autowired
    private CategoryRepository categoryRepository;

    // 1. API Lấy danh sách danh mục
    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // 2. API Lấy danh sách sân (Có hỗ trợ lọc theo categoryId)
    @GetMapping
    public List<Court> getAll(@RequestParam(required = false) Long categoryId) {
        List<Court> courts = courtService.getAllCourts();
        if (categoryId != null) {
            return courts.stream()
                    .filter(court -> court.getCategory() != null && court.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
        }
        return courts;
    }

    // 3. API Tìm kiếm sân
    @GetMapping("/search")
    public List<Court> search(@RequestParam String keyword) {
        return courtService.searchCourts(keyword);
    }

    // 4. API Sân nổi bật (Top 4)
    @GetMapping("/top")
    public List<Court> getTopCourts() {
        return courtService.getTopCourts();
    }

    // 5. API Lấy chi tiết 1 sân
    @GetMapping("/{id}")
    public ResponseEntity<Court> getCourtById(@PathVariable Long id) {
        Court court = courtService.getCourtById(id);
        if (court != null) {
            return ResponseEntity.ok(court);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // --- CÁC API THÊM - SỬA - XÓA (ADMIN) ---

    // 6. Thêm sân mới (Dùng CourtRequest để xử lý Category)
    @PostMapping
    public Court createCourt(@RequestBody CourtRequest request) {
        return courtService.createCourt(request);
    }

    // 7. Sửa sân (Dùng CourtRequest)
    @PutMapping("/{id}")
    public Court updateCourt(@PathVariable Long id, @RequestBody CourtRequest request) {
        return courtService.updateCourt(id, request);
    }

    // 8. Xóa sân
    @DeleteMapping("/{id}")
    public String deleteCourt(@PathVariable Long id) {
        courtService.deleteCourt(id);
        return "Đã xóa sân có ID: " + id;
    }
}