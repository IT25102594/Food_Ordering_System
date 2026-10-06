package com.example.food_ordering_system.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class CartItemDto {
    private Integer cartItemId;
    private Integer foodVariantId;
    private String itemName;
    private String variantName;
    private BigDecimal price;
    private Integer quantity;
    private String specialInstructions;
    private String imageUrl;
}