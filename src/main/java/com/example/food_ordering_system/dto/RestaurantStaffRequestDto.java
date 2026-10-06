package com.example.food_ordering_system.dto;

import lombok.Data;

@Data
public class RestaurantStaffRequestDto {
    private String email;
    private String roleType;
}