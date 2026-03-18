package com.football.booking_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import com.football.booking_system.service.BookingService;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/vnpay-return")
    public void vnpayReturn(@RequestParam Map<String, String> queryParams, HttpServletResponse response) throws IOException {
        String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
        String vnp_TxnRef = queryParams.get("vnp_TxnRef");

        // --- DEBUG LOG (Xem ở Console Backend) ---
        System.out.println("-------------------------------------------------");
        System.out.println("VNPAY RETURN GỌI VỀ:");
        System.out.println("Mã lỗi (ResponseCode): " + vnp_ResponseCode);
        System.out.println("Mã giao dịch (TxnRef): " + vnp_TxnRef);

        Long bookingId = 0L;
        try {
            if (vnp_TxnRef != null) {
                // Thử cắt chuỗi
                String[] parts = vnp_TxnRef.split("_");
                if (parts.length > 0) {
                    bookingId = Long.parseLong(parts[0]);
                    System.out.println("ID Đơn hàng tách được: " + bookingId);
                }
            }
        } catch (Exception e) {
            System.out.println("LỖI: Không đọc được ID đơn hàng! " + e.getMessage());
        }

        if ("00".equals(vnp_ResponseCode)) {
            // THÀNH CÔNG
            System.out.println("=> Giao dịch THÀNH CÔNG. Đang xác nhận đơn...");
            if (bookingId > 0) {
                bookingService.confirmBooking(bookingId);
            }
            response.sendRedirect("https://chuyendethuctap-frontend.vercel.app/payment-success");
            //response.sendRedirect("http://localhost:5173/payment-success");
        } else {
            // THẤT BẠI / HỦY
            System.out.println("=> Giao dịch THẤT BẠI / HỦY. Đang hủy đơn...");
            
            if (bookingId > 0) {
                bookingService.cancelBooking(bookingId); // <--- QUAN TRỌNG
                System.out.println("=> Đã gọi hàm cancelBooking cho ID: " + bookingId);
            } else {
                System.out.println("=> LỖI: Không tìm thấy ID để hủy!");
            }
            
            response.sendRedirect("https://chuyendethuctap-frontend.vercel.app/payment-failed"); 
            //response.sendRedirect("http://localhost:5173/payment-failed");
        }
    }
}