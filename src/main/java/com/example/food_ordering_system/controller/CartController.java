package com.example.food_ordering_system.controller;

import com.example.food_ordering_system.dto.AddToCartRequestDto;
import com.example.food_ordering_system.service.CartService;
import com.example.food_ordering_system.entity.CartItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.food_ordering_system.repository.CartItemRepository;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final CartItemRepository cartItemRepository;

    public CartController(CartService cartService, CartItemRepository cartItemRepository) {
        this.cartService = cartService;
        this.cartItemRepository = cartItemRepository;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestParam Integer userId, @RequestBody AddToCartRequestDto dto) {
        String response = cartService.addToCart(userId, dto);

        if (response.equals("DIFFERENT_RESTAURANT")) {
            // 409 Conflict is ideal here. The frontend can read this status
            // and trigger a "Clear cart to start new order?" modal.
            return ResponseEntity.status(409).body(response);
        } else if (response.startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(@RequestParam Integer userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok("Success: Cart cleared.");
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getCart(@PathVariable Integer userId) {
        try {
            return ResponseEntity.ok(cartService.getCart(userId));
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Cart is empty");
        }
    }

    @PutMapping("/item/{cartItemId}")
    public ResponseEntity<?> updateItemQty(@PathVariable Integer cartItemId, @RequestParam Integer quantity) {
        // Quick method to update qty (remember to validate stock here in production!)
        CartItem item = cartItemRepository.findById(cartItemId).orElseThrow();
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return ResponseEntity.ok("Updated");
    }

    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<?> removeItem(@PathVariable Integer cartItemId) {
        cartItemRepository.deleteById(cartItemId);
        return ResponseEntity.ok("Removed");
    }

    @DeleteMapping("/user/{userId}/item/{variantId}")
    public ResponseEntity<String> removeCartItem(
            @PathVariable Integer userId,
            @PathVariable Integer variantId) {
        try {
            String response = cartService.removeCartItem(userId, variantId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}