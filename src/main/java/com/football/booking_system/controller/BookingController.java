package com.football.booking_system.controller;

import com.football.booking_system.dto.BookingRequest;
import com.football.booking_system.entity.Booking;
import com.football.booking_system.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api/bookings")
@CrossOrigin("*")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // API 1: Xem lịch đã đặt của sân trong ngày (để disable trên frontend)
    // GET: /api/bookings/check?courtId=1&date=2023-11-20
    @GetMapping("/check")
    public List<Booking> checkAvailability(@RequestParam Long courtId, @RequestParam String date) {
        return bookingService.getBookedSlots(courtId, date);
    }

    // API 2: Đặt sân
    // POST: /api/bookings
    @PostMapping
    public ResponseEntity<?> bookCourt(@RequestBody BookingRequest request, HttpServletRequest httpRequest) {
        try {
            // Service trả về Object (có thể là Booking hoặc String URL)
            Object result = bookingService.createBooking(request, httpRequest);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    
    // API 3: Lấy lịch sử đặt sân của User
    // GET: /api/bookings/user/{userId}
    @GetMapping("/user/{userId}")
    public List<Booking> getUserBookings(@PathVariable Long userId) {
        return bookingService.getBookingsByUser(userId);
    }

    // API 4: Hủy đặt sân (User tự hủy)
    // PUT: /api/bookings/cancel/{id}
    @PutMapping("/cancel/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        try {
            // Gọi lại hàm cancelBooking đã viết ở bước trước trong Service
            bookingService.cancelBooking(id); 
            return ResponseEntity.ok("Hủy đặt sân thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi hủy đơn");
        }
    }

    // API 5: Lấy tất cả đơn đặt (ADMIN ONLY)
    // GET: /api/bookings/all
    @GetMapping("/all")
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }
}