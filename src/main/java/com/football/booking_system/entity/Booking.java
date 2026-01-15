package com.football.booking_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ai đặt?
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Sân nào?
    @ManyToOne
    @JoinColumn(name = "court_id")
    private Court court;

    // Ngày đá (Ví dụ: 2023-11-20)
    private LocalDate bookingDate;

    // Giờ bắt đầu (Ví dụ: 17 tức là 17:00)
    private Integer startTime;

    // Giờ kết thúc (Ví dụ: 18 tức là 18:00)
    private Integer endTime;

    // Tổng tiền
    private Double totalPrice;

    private String paymentMethod;

    // Trạng thái: PENDING (Chờ duyệt), CONFIRMED (Đã đặt), CANCELLED (Hủy)
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = BookingStatus.CONFIRMED; // Mặc định là thành công luôn cho lẹ
    }
}