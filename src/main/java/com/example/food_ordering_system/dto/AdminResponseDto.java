package com.example.food_ordering_system.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminResponseDto {
    private Integer adminId;
    private String firstName;
    private String email;
    private Boolean isSuperAdmin;
}