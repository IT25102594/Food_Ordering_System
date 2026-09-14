package com.example.food_ordering_system.controller;

import com.example.food_ordering_system.dto.RestaurantRequestDto;
import com.example.food_ordering_system.dto.RestaurantResponseDto;
import com.example.food_ordering_system.dto.StaffMemberDto;
import com.example.food_ordering_system.service.RestaurantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

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
    public ResponseEntity<String> updateRestaurant(@PathVariable Integer id, @RequestBody RestaurantRequestDto dto) {
        if (restaurantService.updateRestaurant(id, dto)) {
            return ResponseEntity.ok("Success: Restaurant updated.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Restaurant not found.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRestaurant(@PathVariable Integer id) {
        if (restaurantService.deleteRestaurant(id)) {
            return ResponseEntity.ok("Success: Restaurant deleted.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: Restaurant not found.");
    }
    @GetMapping("/{id}/staff")
    public ResponseEntity<List<StaffMemberDto>> getRestaurantStaff(@PathVariable Integer id) {
        return ResponseEntity.ok(restaurantService.getRestaurantStaff(id));
    }
}