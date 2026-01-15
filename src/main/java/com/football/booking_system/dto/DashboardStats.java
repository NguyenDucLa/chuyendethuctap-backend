package com.football.booking_system.dto;
import lombok.Data;

@Data
public class DashboardStats {
    private Long totalRevenue;  // Tổng doanh thu
    private Long totalBookings; // Tổng số đơn
    private Long totalCourts;   // Số sân
    private Long totalUsers;    // Số khách hàng
}