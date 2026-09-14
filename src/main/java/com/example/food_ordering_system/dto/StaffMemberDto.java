package com.example.food_ordering_system.dto;
import lombok.Data;

@Data
public class StaffMemberDto {
    private Integer userId;
    private String firstName;
    private String lastName;
    private String roleType;
}