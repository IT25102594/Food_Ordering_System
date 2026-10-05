package com.example.food_ordering_system.dto;

import lombok.Data;
import java.time.LocalTime;

@Data
public class WorkingHourDto {
    private String dayOfWeek;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Boolean isClosed;
}