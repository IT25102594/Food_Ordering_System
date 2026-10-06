package com.example.food_ordering_system.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CartResponseDto {
    private Integer id;
    private Integer restaurantId;
    private String restaurantName;
    private List<CartItemDto> items;
}