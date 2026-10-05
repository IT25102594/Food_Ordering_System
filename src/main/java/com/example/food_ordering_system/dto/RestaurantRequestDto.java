package com.example.food_ordering_system.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class RestaurantRequestDto {
    private Integer userId;
    private String name;
    private String addressLine;
    private String city;
    private String logoUrl;
    private String bannerUrl;
    private Boolean isOpen;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<WorkingHourDto> workingHours;
}