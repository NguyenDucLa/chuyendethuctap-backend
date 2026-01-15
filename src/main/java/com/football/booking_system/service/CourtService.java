package com.football.booking_system.service;

import com.football.booking_system.dto.CourtRequest;
import com.football.booking_system.entity.Category;
import com.football.booking_system.entity.Court;
import com.football.booking_system.repository.CategoryRepository;
import com.football.booking_system.repository.CourtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourtService {

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // 1. Lấy tất cả sân
    public List<Court> getAllCourts() {
        return courtRepository.findAll();
    }

    // 2. Lấy chi tiết 1 sân
    public Court getCourtById(Long id) {
        return courtRepository.findById(id).orElse(null);
    }

    // 3. Tìm kiếm sân
    public List<Court> searchCourts(String keyword) {
        return courtRepository
                .findByNameContainingIgnoreCaseOrLocationContainingIgnoreCaseOrCategory_NameContainingIgnoreCase(
                        keyword, keyword, keyword);
    }

    // 4. Lấy sân nổi bật
    public List<Court> getTopCourts() {
        return courtRepository.findTop4ByOrderByPricePerHourDesc();
    }

    // 5. Thêm mới sân
    public Court createCourt(CourtRequest request) {
        Court court = new Court();
        return mapToEntity(court, request);
    }

    // 6. Cập nhật sân
    public Court updateCourt(Long id, CourtRequest request) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sân không tồn tại với ID: " + id));
        return mapToEntity(court, request);
    }

    // 7. Xóa sân
    public void deleteCourt(Long id) {
        courtRepository.deleteById(id);
    }

    // 8. Lưu trực tiếp
    public Court saveCourt(Court court) {
        return courtRepository.save(court);
    }

    // --- HÀM PHỤ ---
    private Court mapToEntity(Court court, CourtRequest request) {
        court.setName(request.getName());
        court.setLocation(request.getLocation());
        court.setDescription(request.getDescription());
        court.setPricePerHour(request.getPricePerHour());
        court.setImage(request.getImage());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
            court.setCategory(category);
        }
        
        return courtRepository.save(court);
    }
}