package com.example.food_ordering_system.dto;

import lombok.Data;
import java.util.List;

@Data
public class FoodItemRequestDto {
    private String name;
    private String description;
    private Integer categoryId;

    private List<FoodVariantRequestDto> variants;
}