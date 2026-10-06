package com.example.food_ordering_system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequestDto {
    private Integer variantId;
    private Integer quantity;
    private String specialInstructions;
}