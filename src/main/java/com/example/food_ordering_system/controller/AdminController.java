package com.example.food_ordering_system.controller;

import com.example.food_ordering_system.dto.AdminResponseDto;
import com.example.food_ordering_system.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // GET: http://localhost:8080/api/admins/1 (To view  Super Admin)
    @GetMapping("/{id}")
    public ResponseEntity<AdminResponseDto> getAdmin(@PathVariable Integer id) {
        return ResponseEntity.ok(adminService.getAdminDetails(id));
    }

    // POST: http://localhost:8080/api/admins/promote/2 (To make User 2 a normal admin)
    @PostMapping("/promote/{userId}")
    public ResponseEntity<String> promoteAdmin(@PathVariable Integer userId) {
        return ResponseEntity.ok(adminService.promoteToNormalAdmin(userId));
    }
    @PutMapping("/approve-restaurant/{restaurantId}")
    public ResponseEntity<String> approveRestaurant(
            @PathVariable Integer restaurantId,
            @RequestParam Integer adminId) {
        return ResponseEntity.ok(adminService.approveRestaurant(restaurantId, adminId));
    }
}