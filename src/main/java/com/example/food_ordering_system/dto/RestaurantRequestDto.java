package com.example.food_ordering_system.dto;

import lombok.Data;

@Data
public class RestaurantRequestDto {
    private Integer userId; // Added this!
    private String name;
    private String street;
    private String city;
}