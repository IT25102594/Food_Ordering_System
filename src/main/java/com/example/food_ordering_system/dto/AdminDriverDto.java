package com.example.food_ordering_system.dto;

import lombok.Data;

@Data
public class AdminDriverDto {
    private Integer id;
    private String fullName;
    private String email;
    private String licenseInfo;
    private String approvalStatus;
}