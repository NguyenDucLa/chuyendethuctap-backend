package com.football.booking_system.entity;

import jakarta.persistence.*;
import lombok.Data;
@Entity
@Data
@Table(name = "courts")
public class Court {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String location;
    private String description;
    private Double pricePerHour;
    private String image;

    // Quan trọng: Phải có cái này để biết sân thuộc danh mục nào
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}