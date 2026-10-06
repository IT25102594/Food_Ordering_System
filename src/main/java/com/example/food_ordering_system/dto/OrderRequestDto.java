package com.example.food_ordering_system.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderRequestDto {
    private Integer userId; // Passed manually until we wire up JWT authentication
    private Integer restaurantId;

    // null when the customer dropped a pin instead of picking a saved address
    private Integer deliveryAddressId;
    // the landmark text the customer typed when dropping a pin
    private String deliveryAddressText;

    // where the order is going, from the map pin
    private BigDecimal deliveryLatitude;
    private BigDecimal deliveryLongitude;

    private String deliveryInstructions;
    private List<OrderItemRequestDto> items;
    private List<Integer> appliedItemDiscountIds;

    // the checkout page also sends these, we ignore them until the payment gateway is built
    private String paymentMethod;
    private Integer paymentMethodId;
}