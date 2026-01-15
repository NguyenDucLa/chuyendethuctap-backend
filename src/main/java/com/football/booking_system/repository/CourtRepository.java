package com.football.booking_system.repository;

import com.football.booking_system.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {

    // 1. Tìm kiếm theo Tên Sân HOẶC Vị Trí HOẶC Tên Danh Mục
    List<Court> findByNameContainingIgnoreCaseOrLocationContainingIgnoreCaseOrCategory_NameContainingIgnoreCase(String name, String location, String categoryName);

    // 2. Lấy 4 sân giá cao nhất (Giữ nguyên)
    List<Court> findTop4ByOrderByPricePerHourDesc();

    // ...
    long count(); // Có sẵn rồi, không cần viết thêm
}