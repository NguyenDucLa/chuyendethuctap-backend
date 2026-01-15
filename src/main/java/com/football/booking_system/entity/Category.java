package com.football.booking_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    // Quan trọng: Phải có cái này để map ngược lại (Optional nhưng nên có)
    @OneToMany(mappedBy = "category")
    @JsonIgnore // Chặn lỗi lặp vô tận
    private List<Court> courts;
}