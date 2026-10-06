package com.example.food_ordering_system.factory;

import com.example.food_ordering_system.entity.Delivery;
import java.time.LocalDateTime;

public class DeliveryFactory {

    public static Delivery createDelivery(String orderId, String address) {
        Delivery delivery = new Delivery();
        delivery.setOrderId(orderId);
        delivery.setRiderId("RD-101");
        delivery.setRiderName("Gunarathne R.G.N.S");
        delivery.setCustomerAddress(address);
        delivery.setStatus("ASSIGNED");
        delivery.setCurrentLatitude(6.9271);
        delivery.setCurrentLongitude(79.8612);
        delivery.setUpdatedAt(LocalDateTime.now());

        return delivery;
    }
}