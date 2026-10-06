package com.example.food_ordering_system.dto;

import java.math.BigDecimal;
import java.util.List;

// what the rider page gets on every poll: the delivery they're doing now, plus any open offers
public record DriverJobsDto(Job active, List<Job> offers) {

    // one card: everything the rider needs to decide, and the pins to draw the route
    public record Job(
            Integer orderId,
            String status,
            String restaurantName,
            String restaurantAddress,
            BigDecimal restaurantLat,
            BigDecimal restaurantLng,
            String customerAddress,
            BigDecimal customerLat,
            BigDecimal customerLng,
            String deliveryInstructions,
            BigDecimal driverToRestaurantKm,
            BigDecimal restaurantToCustomerKm,
            BigDecimal deliveryFee,
            int itemCount) {}
}