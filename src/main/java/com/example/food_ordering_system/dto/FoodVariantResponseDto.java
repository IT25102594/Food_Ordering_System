package com.example.food_ordering_system.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FoodVariantResponseDto {
    private Integer id; // Crucial for ordering and updating!
    private String variantName;
    private BigDecimal price;
    private String imageUrl;
    private Integer stockQuantity;
    private Boolean availabilityStatus;
}