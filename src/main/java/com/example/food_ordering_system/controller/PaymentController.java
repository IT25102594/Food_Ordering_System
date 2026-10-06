package com.example.food_ordering_system.controller;

import com.example.food_ordering_system.dto.OrderRequestDto;
import com.example.food_ordering_system.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // what the checkout page sends: the order plus which saved card to charge
    public record PayRequest(OrderRequestDto order, Integer paymentMethodId) { }

    @PostMapping("/pay")
    public ResponseEntity<?> pay(@RequestBody PayRequest req) {
        if (req.order() == null || req.paymentMethodId() == null) {
            return ResponseEntity.badRequest().body("Please choose a card.");
        }
        try {
            return ResponseEntity.ok(paymentService.pay(req.order(), req.paymentMethodId()));
        } catch (RuntimeException e) {
            // messages here are written to be shown to the customer as they are
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}