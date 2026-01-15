package com.football.booking_system.dto;
import lombok.Data;

@Data
public class CourtRequest {
    private String name;
    private String location;
    private String description;
    private Double pricePerHour;
    private String image;
    private Long categoryId; // Quan trọng: chỉ cần gửi ID của danh mục
}