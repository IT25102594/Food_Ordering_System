package com.example.food_ordering_system.controller;

import com.example.food_ordering_system.dto.CheckoutSummaryResponseDto;
import com.example.food_ordering_system.dto.OrderRequestDto;
import com.example.food_ordering_system.dto.OrderResponseDto;
import com.example.food_ordering_system.service.DeliveryService;
import com.example.food_ordering_system.service.OrderService;
import com.example.food_ordering_system.service.DeliveryDriverService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final DeliveryService deliveryService;

    public OrderController(OrderService orderService, DeliveryService deliveryService) {
        this.orderService = orderService;
        this.deliveryService = deliveryService;
    }

    // CREATE: Customer places an order
    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequestDto dto) {
        try {
            String response = orderService.createOrder(dto);
            if (response.startsWith("Error:")) {
                return ResponseEntity.badRequest().body(response);
            }
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // stock / variant problems are thrown so the whole order rolls back, the customer still gets the message
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // READ: Customer views their past orders
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponseDto>> getUserOrders(@PathVariable Integer userId) {
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    // READ: Kitchen dashboard fetching the order queue (supports ?status=placed filtering)
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<OrderResponseDto>> getRestaurantOrders(
            @PathVariable Integer restaurantId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(orderService.getRestaurantOrders(restaurantId, status));
    }

    // UPDATE: Kitchen staff updates order status (e.g. from placed to preparing)
    @PutMapping("/{orderId}/status")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable Integer orderId,
            @RequestParam Integer requesterId,
            @RequestBody Map<String, String> payload) {

        String newStatus = payload.get("status");
        if (newStatus == null || newStatus.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Status is required.");
        }

        String response = orderService.updateOrderStatus(orderId, newStatus, requesterId);
        if (response.startsWith("Error:")) {
            return ResponseEntity.badRequest().body(response);
        }
        if ("ready".equals(newStatus)) deliveryService.sendRequest(orderId);
        return ResponseEntity.ok(response);
    }

    // READ/CALCULATE: Fetches the live checkout math and available discounts
    @PostMapping("/summary")
    public ResponseEntity<?> getCheckoutSummary(@RequestBody OrderRequestDto dto) {
        if (dto.getUserId() == null) {
            return ResponseEntity.badRequest().body("Error: User ID is required to fetch the cart.");
        }

        try {
            CheckoutSummaryResponseDto summary = orderService.getCheckoutSummary(
                    dto.getUserId(),
                    dto.getAppliedItemDiscountIds()
            );
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error calculating summary: " + e.getMessage());
        }
    }
}