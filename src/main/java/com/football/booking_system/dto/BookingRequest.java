package com.football.booking_system.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BookingRequest {
    private Long userId;
    private Long courtId;
    private LocalDate date;
    private Integer startTime; // Ví dụ chọn 17 (17h-18h)
    // Mặc định đá 1 tiếng nên không cần endTime cũng được, hoặc gửi lên cả 2
    private Integer duration; 
    private String paymentMethod;
    
}