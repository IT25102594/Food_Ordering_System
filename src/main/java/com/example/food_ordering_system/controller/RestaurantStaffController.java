package com.example.food_ordering_system.controller;

import com.example.food_ordering_system.dto.RestaurantStaffRequestDto;
import com.example.food_ordering_system.dto.RestaurantStaffResponseDto;
import com.example.food_ordering_system.service.RestaurantStaffService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurant-staff")
public class RestaurantStaffController {

    private final RestaurantStaffService restaurantStaffService;

    public RestaurantStaffController(RestaurantStaffService restaurantStaffService) {
        this.restaurantStaffService = restaurantStaffService;
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<?> getRestaurantStaff(
            @PathVariable Integer restaurantId,
            @RequestParam Integer requesterId) {
        try {
            List<RestaurantStaffResponseDto> staff = restaurantStaffService.getRestaurantStaff(restaurantId, requesterId);
            return ResponseEntity.ok(staff);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search-users")
    public ResponseEntity<?> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(restaurantStaffService.searchUsersForHiring(query));
    }

    @PostMapping("/restaurant/{restaurantId}")
    public ResponseEntity<String> hireRestaurantStaff(
            @PathVariable Integer restaurantId,
            @RequestParam Integer requesterId,
            @RequestBody RestaurantStaffRequestDto dto) {
        String response = restaurantStaffService.hireRestaurantStaff(restaurantId, requesterId, dto);
        if (response.startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/restaurant/{restaurantId}/user/{targetUserId}/role/{roleType}")
    public ResponseEntity<String> fireRestaurantStaff(
            @PathVariable Integer restaurantId,
            @RequestParam Integer requesterId,
            @PathVariable Integer targetUserId,
            @PathVariable String roleType) {
        String response = restaurantStaffService.fireRestaurantStaff(restaurantId, requesterId, targetUserId, roleType);
        if (response.startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    // --- NEW ONLINE ENDPOINTS ---

    @GetMapping("/restaurant/{restaurantId}/online-count")
    public ResponseEntity<Long> getOnlineStaffCount(@PathVariable Integer restaurantId) {
        return ResponseEntity.ok(restaurantStaffService.getOnlineStaffCount(restaurantId));
    }

    @PutMapping("/{userId}/restaurant/{restaurantId}/status")
    public ResponseEntity<String> toggleOnlineStatus(
            @PathVariable Integer userId,
            @PathVariable Integer restaurantId,
            @RequestParam boolean isOnline) {

        String response = restaurantStaffService.toggleOnlineStatus(userId, restaurantId, isOnline);
        if (response.startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{userId}/restaurant/{restaurantId}/status")
    public ResponseEntity<Boolean> getStaffOnlineStatus(
            @PathVariable Integer userId,
            @PathVariable Integer restaurantId) {
        return ResponseEntity.ok(restaurantStaffService.checkUserOnlineStatus(userId, restaurantId));
    }
}