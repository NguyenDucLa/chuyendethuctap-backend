package com.football.booking_system.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.football.booking_system.entity.Booking;
import com.football.booking_system.entity.BookingStatus;
import com.football.booking_system.entity.Court;
import com.football.booking_system.repository.BookingRepository;
import com.football.booking_system.repository.CourtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private BookingRepository bookingRepository; // Inject thêm cái này để soi lịch

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public String processMessage(String userMessage) {
        try {
            // 1. LẤY DỮ LIỆU SÂN BÓNG
            List<Court> courts = courtRepository.findAll();
            StringBuilder dbInfo = new StringBuilder("--- THÔNG TIN SÂN BÓNG HIỆN CÓ ---\n");
            for (Court c : courts) {
                dbInfo.append("- Sân: ").append(c.getName())
                      .append(" (Loại: ").append(c.getCategory() != null ? c.getCategory().getName() : "Đang cập nhật")
                      .append("), Vị trí: ").append(c.getLocation())
                      .append(", Giá: ").append(c.getPricePerHour()).append(" VNĐ/giờ.\n");
            }

            // 2. LẤY DỮ LIỆU LỊCH TRỐNG/KÍN (HÔM NAY VÀ NGÀY MAI)
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);

            List<Booking> todayBookings = bookingRepository.findByBookingDateAndStatusNot(today, BookingStatus.CANCELLED);
            List<Booking> tomorrowBookings = bookingRepository.findByBookingDateAndStatusNot(tomorrow, BookingStatus.CANCELLED);

            dbInfo.append("\n--- TÌNH TRẠNG LỊCH ĐẶT SÂN (RẤT QUAN TRỌNG) ---\n");
            dbInfo.append("Thời gian hoạt động của sân là từ 7:00 sáng đến 22:00 tối mỗi ngày.\n");
            
            // Xử lý lịch hôm nay
            dbInfo.append("1. Hôm nay (Ngày ").append(today).append("):\n");
            if (todayBookings.isEmpty()) {
                dbInfo.append("-> Tuyệt vời! Hôm nay tất cả các sân đều ĐANG TRỐNG mọi khung giờ.\n");
            } else {
                dbInfo.append("-> Các khung giờ ĐÃ BỊ ĐẶT (Không còn trống): ");
                for (Booking b : todayBookings) {
                    dbInfo.append("[").append(b.getCourt().getName()).append(" kín từ ").append(b.getStartTime()).append("h-").append(b.getEndTime()).append("h] ");
                }
                dbInfo.append(". Các khung giờ còn lại đều trống.\n");
            }

            // Xử lý lịch ngày mai
            dbInfo.append("2. Ngày mai (Ngày ").append(tomorrow).append("):\n");
            if (tomorrowBookings.isEmpty()) {
                dbInfo.append("-> Ngày mai tất cả các sân đều ĐANG TRỐNG mọi khung giờ.\n");
            } else {
                dbInfo.append("-> Các khung giờ ĐÃ BỊ ĐẶT (Không còn trống): ");
                for (Booking b : tomorrowBookings) {
                    dbInfo.append("[").append(b.getCourt().getName()).append(" kín từ ").append(b.getStartTime()).append("h-").append(b.getEndTime()).append("h] ");
                }
                dbInfo.append(". Các khung giờ còn lại đều trống.\n");
            }

            // 3. TẠO PROMPT DẠY AI
            String systemPrompt = "Bạn là nhân viên tư vấn nhiệt tình của Hệ thống Đặt Sân Bóng. "
                    + "Hãy trả lời câu hỏi của khách hàng dựa trên thông tin sân và tình trạng lịch đặt sân bên dưới. "
                    + "Ghi chú: Nếu khách hỏi giờ nào đó trống không, hãy nhìn vào danh sách 'Các khung giờ ĐÃ BỊ ĐẶT'. Nếu giờ khách hỏi KHÔNG CÓ trong danh sách bị đặt, nghĩa là giờ đó TRỐNG, hãy mời khách đặt sân. "
                    + "Hãy xưng hô lịch sự (dùng từ 'Dạ', 'ạ'). Nếu khách hỏi ngoài lề, hãy từ chối khéo léo.\n\n"
                    + dbInfo.toString() + "\n\n"
                    + "Câu hỏi của khách: " + userMessage;

            // 4. ĐÓNG GÓI JSON GỬI CHO GOOGLE GEMINI 1.5 FLASH
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", systemPrompt);
            Map<String, Object> parts = new HashMap<>();
            parts.put("parts", List.of(textPart));
            Map<String, Object> contents = new HashMap<>();
            contents.put("contents", List.of(parts));
            
            String requestBody = mapper.writeValueAsString(contents);

            // API URL (Dùng bản 1.5 flash ổn định)
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

        } catch (Exception e) {
            System.out.println("====== LỖI GỌI GEMINI AI ======");
            e.printStackTrace();
            return "Dạ hệ thống AI đang bảo trì. Vui lòng liên hệ hotline 0866981044 để được hỗ trợ ạ!";
        }
    }
}