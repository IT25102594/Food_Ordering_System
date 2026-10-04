package com.example.food_ordering_system.controller;

import com.example.food_ordering_system.dto.FoodVariantRequestDto;
import com.example.food_ordering_system.service.FoodVariantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/food-variants")
public class FoodVariantController {

    private final FoodVariantService foodVariantService;

    public FoodVariantController(FoodVariantService foodVariantService) {
        this.foodVariantService = foodVariantService;
    }

    // CREATE (Add a new variant to an existing food item)
    @PostMapping("/item/{itemId}")
    public ResponseEntity<String> addVariantToItem(
            @PathVariable Integer itemId,
            @RequestParam Integer requesterId,
            @RequestBody FoodVariantRequestDto dto) {
        String response = foodVariantService.addVariant(itemId, requesterId, dto);
        if (response.startsWith("Error:")) return ResponseEntity.badRequest().body(response);
        return ResponseEntity.ok(response);
    }

    // READ (Get specific variant details)
    @GetMapping("/{variantId}")
    public ResponseEntity<com.example.food_ordering_system.dto.FoodVariantResponseDto> getVariantById(@PathVariable Integer variantId) {
        com.example.food_ordering_system.dto.FoodVariantResponseDto response = foodVariantService.getVariantById(variantId);
        if (response == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(response);
    }

    // UPDATE (Change price, name, stock, or availability)
    @PutMapping("/{variantId}")
    public ResponseEntity<String> updateVariant(
            @PathVariable Integer variantId,
            @RequestParam Integer requesterId,
            @RequestBody FoodVariantRequestDto dto) {
        String response = foodVariantService.updateVariant(variantId, requesterId, dto);
        if (response.startsWith("Error:")) return ResponseEntity.badRequest().body(response);
        return ResponseEntity.ok(response);
    }

    // DELETE (Remove a specific variant)
    @DeleteMapping("/{variantId}")
    public ResponseEntity<String> deleteVariant(
            @PathVariable Integer variantId,
            @RequestParam Integer requesterId) {
        String response = foodVariantService.deleteVariant(variantId, requesterId);
        if (response.startsWith("Error:")) return ResponseEntity.badRequest().body(response);
        return ResponseEntity.ok(response);
    }
}