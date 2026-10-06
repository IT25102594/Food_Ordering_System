package com.example.food_ordering_system.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CheckoutSummaryResponseDto {
    private BigDecimal subtotal;                 // before discounts
    private BigDecimal discountTotal;            // item discounts taken off the subtotal
    private BigDecimal deliveryFee;              // after the delivery discount
    private BigDecimal deliveryDiscountPercent;  // 0 when no delivery discount applies
    private BigDecimal deliveryDiscountAmount;
    private BigDecimal grandTotal;

    // The backend sends back the available discounts so the frontend can draw the manual selection cards
    private List<AvailableDiscountDto> availableItemDiscounts;
}