package com.football.booking_system.controller;

import com.football.booking_system.dto.DashboardStats;
import com.football.booking_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

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
    
    // API: Lấy thống kê doanh thu theo tháng
    // GET: http://localhost:8080/api/admin/revenue-chart
    @GetMapping("/revenue-chart")
    public List<Map<String, Object>> getRevenueChart() {
        // 1. Lấy dữ liệu thô từ DB (chỉ có những tháng có doanh thu)
        List<Object[]> results = bookingRepository.getMonthlyRevenue();

        // 2. Tạo một Map để dễ tra cứu (Key: Tháng, Value: Tiền)
        Map<Integer, Double> revenueMap = new HashMap<>();
        for (Object[] row : results) {
            Integer month = (Integer) row[0];
            Double total = (Double) row[1];
            revenueMap.put(month, total);
        }

        // 3. Tạo danh sách đủ 12 tháng (Tháng nào thiếu thì điền 0)
        List<Map<String, Object>> chartData = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", "Tháng " + i);
            // Nếu tháng i có trong map thì lấy tiền, không thì lấy 0
            item.put("doanhThu", revenueMap.getOrDefault(i, 0.0));
            chartData.add(item);
        }

        return chartData;
    }
}