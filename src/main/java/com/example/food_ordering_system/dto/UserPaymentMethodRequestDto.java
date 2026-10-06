package com.example.food_ordering_system.dto;

import lombok.Data;

@Data
public class UserPaymentMethodRequestDto {
    private String label;
    private String paymentToken;
    private String cardBrand;
    private String lastFourDigits;
    private Integer expMonth;
    private Integer expYear;
    private Boolean isDefault;
}
