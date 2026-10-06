package com.example.food_ordering_system.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemResponseDto {
    private Integer orderItemId;
    private Integer variantId;
    private String itemName;
    private String variantName;
    private Integer quantity;
    private BigDecimal priceAtOrderTime;
    private BigDecimal subtotal;
    private String specialInstructions;
}