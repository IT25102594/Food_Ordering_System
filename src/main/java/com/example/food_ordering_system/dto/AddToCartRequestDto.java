package com.example.food_ordering_system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddToCartRequestDto {
    private Integer restaurantId;
    private Integer variantId;
    private Integer quantity;
    private String specialInstructions;
}