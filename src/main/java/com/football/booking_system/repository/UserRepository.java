package com.football.booking_system.repository;

import com.football.booking_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Tìm user bằng email
    Optional<User> findByEmail(String email);
    
    // Kiểm tra email đã tồn tại chưa
    boolean existsByEmail(String email);

    // ...
    // Đếm số user có role là USER
    long countByRole(String role);
}