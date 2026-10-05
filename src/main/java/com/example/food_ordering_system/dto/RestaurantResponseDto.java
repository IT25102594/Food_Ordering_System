package com.example.food_ordering_system.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class RestaurantResponseDto {
    private Integer id;
    private String name;
    private String addressLine;
    private String city;
    private BigDecimal avgRating;
    private String approvalStatus;
    private String logoUrl;
    private String bannerUrl;
    private Boolean isOpen;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<WorkingHourDto> workingHours;
}