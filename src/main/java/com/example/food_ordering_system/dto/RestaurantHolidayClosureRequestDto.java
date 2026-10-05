package com.example.food_ordering_system.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class RestaurantHolidayClosureRequestDto {
    private Integer restaurantId;
    private LocalDate exceptionDate;
    private String reason;
    private Boolean isFullyClosed;
    private LocalTime customOpenTime;
    private LocalTime customCloseTime;
}