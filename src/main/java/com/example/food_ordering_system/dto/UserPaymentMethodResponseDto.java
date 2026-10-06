package com.example.food_ordering_system.dto;

import lombok.Data;

@Data
public class UserPaymentMethodResponseDto {
    private Integer id;
    private String label;
    private String gatewayProvider;
    private String cardBrand;
    private String lastFourDigits;
    private Integer expMonth;
    private Integer expYear;
    private Boolean isDefault;
    // deleted the Instant createdAt field
}