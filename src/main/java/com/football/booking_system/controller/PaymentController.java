package com.football.booking_system.controller;

// 1. Import các thư viện của Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

// 2. Import các thư viện Java cơ bản
import java.io.IOException;
import java.util.Map;

// 3. Import Service xử lý logic
import com.football.booking_system.service.BookingService;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*") // Cho phép Frontend gọi API thoải mái
public class PaymentController {

    @Autowired
    private BookingService bookingService;

    // API nhận phản hồi từ VNPAY (Callback)
    // URL: http://localhost:8080/api/payment/vnpay-return
    @GetMapping("/vnpay-return")
    public void vnpayReturn(@RequestParam Map<String, String> queryParams, HttpServletResponse response) throws IOException {
        String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
        String vnp_TxnRef = queryParams.get("vnp_TxnRef"); // Mã đơn hàng dạng: ID_Random

        // BƯỚC 1: Tách lấy ID booking từ chuỗi ref (Ví dụ: "15_9823" -> lấy số 15)
        // Lấy ra trước để dùng cho cả 2 trường hợp thành công hoặc thất bại
        Long bookingId = 0L;
        try {
            if (vnp_TxnRef != null) {
                bookingId = Long.parseLong(vnp_TxnRef.split("_")[0]);
            }
        } catch (NumberFormatException e) {
            System.out.println("Lỗi đọc mã đơn hàng: " + e.getMessage());
        }

        // BƯỚC 2: Kiểm tra kết quả giao dịch
        if ("00".equals(vnp_ResponseCode)) {
            // --- TRƯỜNG HỢP: THANH TOÁN THÀNH CÔNG ---
            if (bookingId > 0) {
                bookingService.confirmBooking(bookingId); // Cập nhật thành CONFIRMED
            }
            // Chuyển hướng về trang báo Thành công
            response.sendRedirect("http://localhost:5173/payment-success");
            
        } else {
            // --- TRƯỜNG HỢP: THANH TOÁN THẤT BẠI / HỦY BỎ ---
            if (bookingId > 0) {
                bookingService.cancelBooking(bookingId); // Cập nhật thành CANCELLED để giải phóng sân
            }
            // Chuyển hướng về trang báo Thất bại
            response.sendRedirect("http://localhost:5173/payment-failed"); 
        }
    }
}