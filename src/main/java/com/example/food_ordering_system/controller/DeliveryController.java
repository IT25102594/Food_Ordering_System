package com.example.food_ordering_system.controller;

import com.example.food_ordering_system.entity.Delivery;
import com.example.food_ordering_system.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deliveries")
@CrossOrigin(origins = "*")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @PostMapping("/assign")
    public ResponseEntity<Delivery> assignRider(@RequestParam String orderId, @RequestParam String address) {
        Delivery delivery = deliveryService.assignRider(orderId, address);
        return ResponseEntity.ok(delivery);
    }

    @PutMapping("/update-status")
    public ResponseEntity<?> updateStatus(@RequestParam String orderId, @RequestParam String status) {
        Delivery updated = deliveryService.updateStatus(orderId, status);
        if (updated != null) {
            return ResponseEntity.ok("Status updated successfully to: " + status);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/track/{orderId}")
    public ResponseEntity<Delivery> trackOrder(@PathVariable String orderId) {
        Delivery delivery = deliveryService.trackOrder(orderId);
        if (delivery != null) {
            return ResponseEntity.ok(delivery);
        }
        return ResponseEntity.notFound().build();
    }
}