package com.football.booking_system.controller;

import com.football.booking_system.dto.LoginRequest;
import com.football.booking_system.dto.RegisterRequest;
import com.football.booking_system.entity.User;
import com.football.booking_system.repository.UserRepository;
import com.football.booking_system.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    // Mã hóa mật khẩu
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 1. API ĐĂNG KÝ (Đã thêm @Valid và BindingResult)
    @PostMapping("/register")
    public Map<String, String> register(@Valid @RequestBody RegisterRequest request, BindingResult result) {
        Map<String, String> response = new HashMap<>();

        // 1. Kiểm tra lỗi Validate (Email sai, pass ngắn...)
        if (result.hasErrors()) {
            // Lấy lỗi đầu tiên để trả về cho gọn
            String errorMessage = result.getFieldError().getDefaultMessage();
            response.put("message", errorMessage);
            return response; // Trả về lỗi ngay
        }

        // 2. Kiểm tra email trùng trong DB
        if (userRepository.existsByEmail(request.getEmail())) {
            response.put("message", "Email đã tồn tại!");
            return response;
        }

        // 3. Logic tạo user (Giữ nguyên)
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        response.put("message", "Đăng ký thành công!");
        return response;
    }

    // 2. API ĐĂNG NHẬP
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();

        // Tìm user trong DB
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        // Nếu có user và Mật khẩu khớp nhau
        if (userOpt.isPresent() && passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            User user = userOpt.get();
            
            // Sinh ra Token
            String token = jwtUtils.generateToken(user.getEmail());

            // Trả về Token và thông tin user
            response.put("token", token);
            response.put("role", user.getRole());
            response.put("fullName", user.getFullName());
            response.put("id", user.getId());
            return response;
        } else {
            response.put("message", "Sai email hoặc mật khẩu!");
            return response;
        }
    }
}