package com.example.food_ordering_system.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class FoodItemResponseDto {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal avgRating;
    private BigDecimal defaultPrice;
    private String defaultImageUrl;
    private Integer categoryId;
    private String categoryName;

    // Now uses the Response DTO so the frontend gets the IDs
    private List<FoodVariantResponseDto> variants;
}