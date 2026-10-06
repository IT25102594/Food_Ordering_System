package com.example.food_ordering_system.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class RestaurantStaffResponseDto {
    private Integer userId;
    private String name;
    private String email;
    private String roleType;
    private Instant assignedAt;
}