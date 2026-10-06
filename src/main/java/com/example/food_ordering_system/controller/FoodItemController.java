package com.example.food_ordering_system.controller;

import com.example.food_ordering_system.dto.FoodItemRequestDto;
import com.example.food_ordering_system.dto.FoodItemResponseDto;
import com.example.food_ordering_system.service.FoodItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/food-items")
public class FoodItemController {

    private final FoodItemService foodItemService;

    public FoodItemController(FoodItemService foodItemService) {
        this.foodItemService = foodItemService;
    }

    // CREATE (Already built in service)
    @PostMapping("/restaurant/{restaurantId}")
    public ResponseEntity<String> createFoodItem(
            @PathVariable Integer restaurantId,
            @RequestParam Integer requesterId,
            @RequestBody FoodItemRequestDto dto) {
        String response = foodItemService.createFoodItem(restaurantId, requesterId, dto);
        if (response.startsWith("Error:")) return ResponseEntity.badRequest().body(response);
        return ResponseEntity.ok(response);
    }

    // READ ALL (For a specific restaurant menu)
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<FoodItemResponseDto>> getMenuByRestaurant(@PathVariable Integer restaurantId) {
        return ResponseEntity.ok(foodItemService.getMenuByRestaurant(restaurantId));
    }

    // READ ONE (Get specific item details)
    @GetMapping("/{itemId}")
    public ResponseEntity<FoodItemResponseDto> getFoodItemById(@PathVariable Integer itemId) {
        FoodItemResponseDto response = foodItemService.getFoodItemById(itemId);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(response);
    }

    // UPDATE (Update item name, description, image, or category)
    @PutMapping("/{itemId}")
    public ResponseEntity<String> updateFoodItem(
            @PathVariable Integer itemId,
            @RequestParam Integer requesterId,
            @RequestBody FoodItemRequestDto dto) {
        String response = foodItemService.updateFoodItem(itemId, requesterId, dto);
        if (response.startsWith("Error:")) return ResponseEntity.badRequest().body(response);
        return ResponseEntity.ok(response);
    }

    // DELETE (Will cascade delete its variants)
    @DeleteMapping("/{itemId}")
    public ResponseEntity<String> deleteFoodItem(
            @PathVariable Integer itemId,
            @RequestParam Integer requesterId) {
        String response = foodItemService.deleteFoodItem(itemId, requesterId);
        if (response.startsWith("Error:")) return ResponseEntity.badRequest().body(response);
        return ResponseEntity.ok(response);
    }
}