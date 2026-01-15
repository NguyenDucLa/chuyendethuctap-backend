package com.football.booking_system.controller;

import com.football.booking_system.dto.DashboardStats;
import com.football.booking_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class AdminController {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private CourtRepository courtRepository;
    @Autowired
    private UserRepository userRepository;

    // API lấy thống kê Dashboard
    // GET: http://localhost:8080/api/admin/stats
    @GetMapping("/stats")
    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();

        // 1. Tổng doanh thu (Nếu null thì trả về 0)
        Double revenue = bookingRepository.sumTotalRevenue();
        stats.setTotalRevenue(revenue != null ? revenue.longValue() : 0L);

        // 2. Tổng số đơn
        stats.setTotalBookings(bookingRepository.count());

        // 3. Tổng số sân
        stats.setTotalCourts(courtRepository.count());

        // 4. Tổng số khách hàng (Role = USER)
        stats.setTotalUsers(userRepository.countByRole("USER"));

        return stats;
    }
}