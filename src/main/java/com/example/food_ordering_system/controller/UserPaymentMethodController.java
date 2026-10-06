package com.example.food_ordering_system.controller;

import com.example.food_ordering_system.dto.UserPaymentMethodRequestDto;
import com.example.food_ordering_system.dto.UserPaymentMethodResponseDto;
import com.example.food_ordering_system.service.UserPaymentMethodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/users/{userId}/payment-methods")
@RequiredArgsConstructor
public class UserPaymentMethodController {

    private final UserPaymentMethodService paymentMethodService;

    @GetMapping
    public ResponseEntity<List<UserPaymentMethodResponseDto>> getPaymentMethods(
            @PathVariable Integer userId) {
        return ResponseEntity.ok(paymentMethodService.getUserPaymentMethods(userId));
    }

    @PostMapping
    public ResponseEntity<UserPaymentMethodResponseDto> addPaymentMethod(
            @PathVariable Integer userId,
            @Valid @RequestBody UserPaymentMethodRequestDto requestDto) {
        UserPaymentMethodResponseDto savedMethod = paymentMethodService.addPaymentMethod(userId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMethod);
    }

    @DeleteMapping("/{methodId}")
    public ResponseEntity<Void> deletePaymentMethod(
            @PathVariable Integer userId,
            @PathVariable Integer methodId) {
        paymentMethodService.deletePaymentMethod(userId, methodId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{methodId}/default")
    public ResponseEntity<UserPaymentMethodResponseDto> setAsDefault(
            @PathVariable Integer userId,
            @PathVariable Integer methodId) {
        return ResponseEntity.ok(paymentMethodService.setAsDefault(userId, methodId));
    }
}