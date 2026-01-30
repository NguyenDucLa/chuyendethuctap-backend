package com.football.booking_system.service;

import com.football.booking_system.entity.Booking;
import com.football.booking_system.entity.BookingStatus;
import com.football.booking_system.entity.Court;
import com.football.booking_system.entity.UnansweredMessage;
import com.football.booking_system.repository.BookingRepository;
import com.football.booking_system.repository.CourtRepository;
import com.football.booking_system.repository.UnansweredMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private BookingRepository bookingRepository; // Dùng để soi lịch

    @Autowired
    private UnansweredMessageRepository unansweredMessageRepository; // Dùng để lưu câu hỏi lạ

    public String processMessage(String message) {
        String msg = message.toLowerCase();

        // 1. Hỏi về DANH SÁCH SÂN
        if (msg.contains("sân") && (msg.contains("nào") || msg.contains("danh sách") || msg.contains("có"))) {
            List<Court> courts = courtRepository.findAll();
            if (courts.isEmpty()) return "Hiện tại hệ thống chưa có sân nào hoạt động ạ.";
            String names = courts.stream().limit(3).map(Court::getName).collect(Collectors.joining(", "));
            return "Hệ thống hiện có " + courts.size() + " sân. Ví dụ: " + names + "...";
        }

        // 2. Hỏi về GIÁ
        if (msg.contains("giá") || msg.contains("tiền")) {
            return "Giá sân dao động từ 150k - 450k/giờ tùy loại sân bạn nhé!";
        }

        // 3. Hỏi về ĐỊA CHỈ
        if (msg.contains("địa chỉ") || msg.contains("ở đâu")) {
            return "Sân bóng nằm tại 12 Nguyễn Văn Bảo, Gò Vấp, TP.HCM ạ.";
        }

        // --- 4. TÍNH NĂNG MỚI: Check Sân Trống Hôm Nay ---
        if (msg.contains("trống") || msg.contains("lịch") || msg.contains("hôm nay")) {
            LocalDate today = LocalDate.now();
            // Đếm số đơn đặt hôm nay (Trừ đơn hủy)
            List<Booking> bookings = bookingRepository.findByBookingDateAndStatusNot(today, BookingStatus.CANCELLED);
            int count = bookings.size();

            if (count == 0) {
                return "Hôm nay (" + today + ") sân đang RẤT TRỐNG (chưa có ai đặt). Bạn đặt ngay đi!";
            } else if (count < 5) {
                return "Hôm nay (" + today + ") mới có " + count + " lịch đặt thôi, vẫn còn nhiều giờ đẹp lắm.";
            } else {
                return "Hôm nay (" + today + ") khá đông (" + count + " lịch rồi). Bạn vào xem chi tiết để chọn giờ còn lại nhé.";
            }
        }
        
        // 5. Chào hỏi
        if (msg.contains("hi") || msg.contains("chào")) {
            return "Chào bạn! Mình là AI hỗ trợ đặt sân. Bạn cần tìm gì?";
        }

        // --- 6. CƠ CHẾ HỌC: Nếu không hiểu -> Lưu vào DB ---
        // Lưu câu hỏi này lại để Admin đọc và dạy Bot sau
        UnansweredMessage unknown = new UnansweredMessage(message);
        unansweredMessageRepository.save(unknown);

        return "Xin lỗi, câu này mình chưa được học. Mình đã ghi lại để Admin trả lời bạn sau nhé! Bạn thử hỏi về 'giá' hoặc 'sân trống' xem?";
    }
}