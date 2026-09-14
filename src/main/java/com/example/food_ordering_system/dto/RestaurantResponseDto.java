package com.example.food_ordering_system.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RestaurantResponseDto {
    private Integer id;
    private String name;
    private String street;
    private String city;
    private BigDecimal avgRating;
    private String approvalStatus;
}