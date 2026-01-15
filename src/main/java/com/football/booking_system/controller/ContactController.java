package com.football.booking_system.controller;

import com.football.booking_system.entity.Contact;
import com.football.booking_system.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin("*")
public class ContactController {

    @Autowired
    private ContactRepository contactRepository;

    // 1. Gửi liên hệ (Ai cũng gửi được)
    @PostMapping
    public Contact sendContact(@RequestBody Contact contact) {
        return contactRepository.save(contact);
    }

    // 2. Xem danh sách liên hệ (Chỉ Admin - Sẽ chặn ở Security)
    @GetMapping
    public List<Contact> getAllContacts() {
        // Sắp xếp tin mới nhất lên đầu
        List<Contact> list = contactRepository.findAll();
        list.sort((a, b) -> b.getId().compareTo(a.getId()));
        return list;
    }
}