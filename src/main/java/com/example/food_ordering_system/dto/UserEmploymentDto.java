package com.example.food_ordering_system.dto;
import lombok.Data;

@Data
public class UserEmploymentDto {
    private Integer restaurantId;
    private String restaurantName;
    private String roleType;
}