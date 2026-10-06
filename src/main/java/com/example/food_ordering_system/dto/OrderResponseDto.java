package com.example.food_ordering_system.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class OrderResponseDto {
    private Integer orderId;
    private Integer restaurantId;
    private String restaurantName;
    private String status;
    private Instant placedAt;
    private String deliveryAddressSnapshot;
    private String deliveryInstructions;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private BigDecimal deliveryLatitude;
    private BigDecimal deliveryLongitude;
    private Integer driverId;
    private List<OrderItemResponseDto> items;
}