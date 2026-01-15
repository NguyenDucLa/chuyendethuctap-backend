package com.football.booking_system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity // Đánh dấu đây là table trong DB
@Data   // Lombok tự sinh Getter/Setter
@Table(name = "users") // Đặt tên bảng là 'users'
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tự tăng ID
    private Long id;

    @Column(unique = true) // Không được trùng email
    private String email;

    private String password;

    private String fullName;

    private String phone;
    
    private String role; // LƯU 'ADMIN' hoặc 'USER'

    private String address;
}