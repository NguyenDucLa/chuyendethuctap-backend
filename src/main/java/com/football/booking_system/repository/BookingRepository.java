package com.football.booking_system.repository;

import com.football.booking_system.entity.Booking;
import com.football.booking_system.entity.BookingStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Tìm các lịch đã đặt của 1 sân trong ngày cụ thể (để tô màu đỏ trên frontend)
    // SELECT * FROM bookings WHERE court_id = ? AND booking_date = ? AND status !=
    // 'CANCELLED'
    List<Booking> findByCourtIdAndBookingDateAndStatusNot(Long courtId, LocalDate date, BookingStatus status);

    // Tìm lịch sử đặt của 1 user
    List<Booking> findByUserId(Long userId);

    // Tính tổng tiền các đơn đã CONFIRMED (Thành công)
    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.status = 'CONFIRMED'")
    Double sumTotalRevenue();

    // Đếm tổng số đơn
    long count();
}