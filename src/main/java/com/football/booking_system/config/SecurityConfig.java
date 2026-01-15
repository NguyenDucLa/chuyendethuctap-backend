package com.football.booking_system.config;

import com.football.booking_system.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration; // Import mới
import org.springframework.web.cors.CorsConfigurationSource; // Import mới
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // Import mới

import java.util.List; // Import mới

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // --- 1. KHU VỰC PUBLIC (Ai cũng vào được) ---
                .requestMatchers("/api/auth/**").permitAll()
                
                // Sân bóng & Tìm kiếm (GET là public)
                .requestMatchers(HttpMethod.GET, "/api/courts/**").permitAll() 
                
                // Danh mục (GET là public)
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                
                // Thanh toán & Tiện ích khác
                .requestMatchers("/api/payment/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/contacts").permitAll() // Khách gửi liên hệ
                .requestMatchers("/api/bookings/check").permitAll() // Khách xem lịch trống

                // --- 2. KHU VỰC ADMIN (Chỉ Admin mới được vào) ---
                
                // Dashboard Admin
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Quản lý Sân (Thêm, Sửa, Xóa)
                .requestMatchers(HttpMethod.POST, "/api/courts/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/courts/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/courts/**").hasRole("ADMIN")

                // Quản lý Danh mục (Thêm, Sửa, Xóa) - Lưu ý: GET đã được allow ở trên
                .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")

                // Quản lý Đặt lịch & Phản hồi
                .requestMatchers("/api/bookings/all").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/contacts").hasRole("ADMIN")

                // Quản lý User (Lấy tất cả, Xóa, THÊM MỚI)
                .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN") // <--- QUAN TRỌNG: Chặn user thường tạo user mới

                // --- 3. KHU VỰC ĐĂNG NHẬP (User & Admin đều được) ---
                // Bao gồm: Đặt sân, Xem lịch sử, Xem Profile, Đổi mật khẩu...
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // --- CẤU HÌNH CORS CỤ THỂ ---
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Cho phép port của Frontend (5173)
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        // Cho phép các method
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Cho phép các header (đặc biệt là Authorization)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "x-auth-token"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}