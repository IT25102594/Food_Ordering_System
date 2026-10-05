package com.example.food_ordering_system.controller;

import com.example.food_ordering_system.dto.RestaurantHolidayClosureRequestDto;
import com.example.food_ordering_system.entity.RestaurantHolidayClosure;
import com.example.food_ordering_system.service.RestaurantHolidayClosureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/holiday-closures")
public class RestaurantHolidayClosureController {

    private final RestaurantHolidayClosureService closureService;

    public RestaurantHolidayClosureController(RestaurantHolidayClosureService closureService) {
        this.closureService = closureService;
    }

    @PostMapping("/restaurant/{restaurantId}")
    public ResponseEntity<String> createClosure(
            @PathVariable Integer restaurantId,
            @RequestParam Integer requesterId,
            @RequestBody RestaurantHolidayClosureRequestDto dto) {

        dto.setRestaurantId(restaurantId); // Override in case JSON doesn't match URL
        String response = closureService.createClosure(requesterId, dto);

        if (response.startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<RestaurantHolidayClosure>> getClosuresForRestaurant(@PathVariable Integer restaurantId) {
        return ResponseEntity.ok(closureService.getClosuresForRestaurant(restaurantId));
    }

    @DeleteMapping("/{closureId}/restaurant/{restaurantId}")
    public ResponseEntity<String> deleteClosure(
            @PathVariable Integer closureId,
            @PathVariable Integer restaurantId,
            @RequestParam Integer requesterId) {

        String response = closureService.deleteClosure(closureId, requesterId, restaurantId);
        if (response.startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}