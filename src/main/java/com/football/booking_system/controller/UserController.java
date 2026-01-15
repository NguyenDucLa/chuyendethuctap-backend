package com.football.booking_system.controller;

import com.football.booking_system.entity.User;
import com.football.booking_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.football.booking_system.dto.UpdateProfileRequest;
import com.football.booking_system.dto.ChangePasswordRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // API lấy thông tin chi tiết user theo ID
    // GET: http://localhost:8080/api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            // Mẹo nhỏ: Set password null trước khi trả về để bảo mật (không lộ hash mật
            // khẩu)
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // API 2: Cập nhật thông tin cá nhân
    // PUT: http://localhost:8080/api/users/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody UpdateProfileRequest request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        // 1. Kiểm tra nếu người dùng CÓ gửi email mới và email đó KHÁC email hiện tại
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // Kiểm tra xem email mới này đã có ai dùng chưa
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body("Email này đã được sử dụng bởi tài khoản khác!");
            }
            // Nếu chưa ai dùng -> Cho phép đổi
            user.setEmail(request.getEmail());
        }

        // 2. Cập nhật các thông tin khác
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        
        userRepository.save(user);
        return ResponseEntity.ok("Cập nhật thành công!");
    }

    // API 3: Đổi mật khẩu
    // PUT: http://localhost:8080/api/users/{id}/password
    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequest request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        // 1. Kiểm tra mật khẩu cũ có đúng không
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu cũ không chính xác!");
        }

        // 2. Mã hóa và lưu mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Đổi mật khẩu thành công!");
    }

    // API 4: Lấy tất cả User (Chỉ Admin)
    // GET: http://localhost:8080/api/users
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // API 5: Xóa User (Chỉ Admin)
    // DELETE: http://localhost:8080/api/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        // Lưu ý: Nếu user này đã có booking, việc xóa có thể gây lỗi khóa ngoại (Foreign Key).
        // Tốt nhất là nên xóa booking của họ trước, hoặc dùng Soft Delete (cờ active=false).
        // Ở mức độ đồ án này, ta cứ cho xóa, nếu lỗi thì try-catch báo lỗi.
        try {
            userRepository.deleteById(id);
            return ResponseEntity.ok("Đã xóa thành công user ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể xóa user này (Do đã có dữ liệu đặt sân).");
        }
    }
    
}