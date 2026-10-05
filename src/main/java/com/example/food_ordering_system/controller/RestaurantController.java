package com.example.food_ordering_system.controller;

import com.example.food_ordering_system.dto.RestaurantRequestDto;
import com.example.food_ordering_system.dto.RestaurantResponseDto;
import com.example.food_ordering_system.dto.StaffMemberDto;
import com.example.food_ordering_system.dto.UserEmploymentDto;
import com.example.food_ordering_system.service.RestaurantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    // --- Restaurant CRUD ---

    @PostMapping
    public ResponseEntity<String> createRestaurant(@RequestBody RestaurantRequestDto dto) {
        String result = restaurantService.createRestaurant(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponseDto>> getAllRestaurants() {
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponseDto> getRestaurantById(@PathVariable Integer id) {
        RestaurantResponseDto restaurant = restaurantService.getRestaurantById(id);
        if (restaurant != null) {
            return ResponseEntity.ok(restaurant);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateRestaurant(
            @PathVariable Integer id,
            @RequestParam Integer requesterId, // Added this!
            @RequestBody RestaurantRequestDto dto) {
        String response = restaurantService.updateRestaurant(id, requesterId, dto);
        if (response.startsWith("Success:")) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRestaurant(@PathVariable Integer id) {
        if (restaurantService.deleteRestaurant(id)) {
            return ResponseEntity.ok("Success: Restaurant deleted.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Restaurant not found.");
    }

    // --- Restaurant Staff ---

    @GetMapping("/{id}/staff")
    public ResponseEntity<List<StaffMemberDto>> getRestaurantStaff(@PathVariable Integer id) {
        return ResponseEntity.ok(restaurantService.getRestaurantStaff(id));
    }
    @PostMapping("/{id}/staff")
    public ResponseEntity<String> assignStaff(
            @PathVariable Integer id,
            @RequestParam Integer requesterId, // The ID of the logged-in user making the request
            @RequestBody UserEmploymentDto dto) {
        return ResponseEntity.ok(restaurantService.assignStaffToRestaurant(id, requesterId, dto));
    }

    // --- Admin Approval ---

    @PutMapping("/{restaurantId}/approve")
    public ResponseEntity<String> approveRestaurant(
            @PathVariable Integer restaurantId,
            @RequestParam Integer adminId) {
        return ResponseEntity.ok(restaurantService.approveRestaurant(restaurantId, adminId));
    }

}