package com.football.booking_system.service;

import com.football.booking_system.dto.BookingRequest;
import com.football.booking_system.entity.*;
import com.football.booking_system.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalTime; // Import thêm để check giờ hiện tại
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private CourtRepository courtRepository;
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PaymentService paymentService;

    // 1. Lấy danh sách giờ đã bị đặt của 1 sân vào ngày X
    public List<Booking> getBookedSlots(Long courtId, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        return bookingRepository.findByCourtIdAndBookingDateAndStatusNot(courtId, date, BookingStatus.CANCELLED);
    }

    // 2. Xử lý đặt sân (ĐÃ NÂNG CẤP: Check ngày quá khứ + Đặt nhiều giờ + VNPAY)
    public Object createBooking(BookingRequest request, HttpServletRequest httpRequest) throws UnsupportedEncodingException {
        
        // --- A. KIỂM TRA NGÀY GIỜ HỢP LỆ (MỚI THÊM) ---
        // 1. Không được đặt ngày trong quá khứ
        if (request.getDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Không thể đặt sân cho ngày trong quá khứ!");
        }

        // 2. Nếu đặt ngày hôm nay -> Không được đặt giờ đã qua
        if (request.getDate().isEqual(LocalDate.now())) {
            int currentHour = LocalTime.now().getHour();
            // Ví dụ: Bây giờ 10h30 (currentHour=10). Khách đặt 10h -> Chặn.
            if (request.getStartTime() <= currentHour) {
                throw new RuntimeException("Khung giờ này đã qua, vui lòng chọn giờ khác!");
            }
        }

        // --- B. XÁC ĐỊNH THỜI LƯỢNG ---
        int duration = (request.getDuration() == null || request.getDuration() <= 0) ? 1 : request.getDuration();
        int endTime = request.getStartTime() + duration;

        // --- C. KIỂM TRA TRÙNG LỊCH ---
        List<Booking> existingBookings = bookingRepository.findByCourtIdAndBookingDateAndStatusNot(
                request.getCourtId(), request.getDate(), BookingStatus.CANCELLED);

        for (int i = 0; i < duration; i++) {
            int checkingHour = request.getStartTime() + i;
            for (Booking b : existingBookings) {
                // Logic: Nếu Start_Cũ <= Giờ_Check < End_Cũ => Bị trùng
                if (checkingHour >= b.getStartTime() && checkingHour < b.getEndTime()) {
                    throw new RuntimeException("Khung giờ " + checkingHour + "h - " + (checkingHour + 1) + "h đã bị người khác đặt rồi!");
                }
            }
        }

        // --- D. TÌM USER VÀ COURT ---
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User không tồn tại (Vui lòng đăng nhập lại)"));
        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new RuntimeException("Sân không tồn tại"));

        // --- E. TẠO ĐƠN MỚI ---
        Booking newBooking = new Booking();
        newBooking.setUser(user);
        newBooking.setCourt(court);
        newBooking.setBookingDate(request.getDate());
        newBooking.setStartTime(request.getStartTime());
        newBooking.setEndTime(endTime);
        newBooking.setTotalPrice(court.getPricePerHour() * duration);
        newBooking.setPaymentMethod(request.getPaymentMethod());

        // --- F. XỬ LÝ THANH TOÁN ---
        if ("VNPAY".equals(request.getPaymentMethod())) {
            newBooking.setStatus(BookingStatus.PENDING);
            Booking savedBooking = bookingRepository.save(newBooking);
            
            // Tạo URL thanh toán VNPAY
            String vnpayUrl = paymentService.createVNPayUrl(savedBooking, httpRequest);
            return vnpayUrl; 
        } else {
            newBooking.setStatus(BookingStatus.CONFIRMED);
            return bookingRepository.save(newBooking);
        }
    }

    // 3. Lấy lịch sử đặt sân của 1 User
    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    // 4. Xác nhận thanh toán thành công
    public void confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking != null) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
        }
    }

    // 5. Hủy đơn
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking != null) {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
        }
    }

    // 6. Lấy TOÀN BỘ lịch đặt (Cho Admin)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }
}