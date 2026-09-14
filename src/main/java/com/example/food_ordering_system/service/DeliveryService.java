package com.example.food_ordering_system.service;

import com.example.food_ordering_system.entity.Delivery;
import com.example.food_ordering_system.repository.DeliveryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    public Delivery assignRider(String orderId, String address) {
        Delivery delivery = new Delivery();
        delivery.setOrderId(orderId);
        delivery.setRiderId("RD-101");
        delivery.setRiderName("Gunarathne R.G.N.S");
        delivery.setCustomerAddress(address);
        delivery.setStatus("ASSIGNED");
        delivery.setCurrentLatitude(6.9271);
        delivery.setCurrentLongitude(79.8612);
        delivery.setUpdatedAt(LocalDateTime.now());

        return deliveryRepository.save(delivery);
    }

    public Delivery updateStatus(String orderId, String status) {
        Optional<Delivery> optionalDelivery = deliveryRepository.findByOrderId(orderId);
        if (optionalDelivery.isPresent()) {
            Delivery delivery = optionalDelivery.get();
            delivery.setStatus(status);
            delivery.setUpdatedAt(LocalDateTime.now());
            return deliveryRepository.save(delivery);
        }
        return null;
    }

    public Delivery trackOrder(String orderId) {
        return deliveryRepository.findByOrderId(orderId).orElse(null);
    }
}
