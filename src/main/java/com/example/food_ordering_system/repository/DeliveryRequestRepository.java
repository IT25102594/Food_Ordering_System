package com.example.food_ordering_system.repository;

import com.example.food_ordering_system.entity.DeliveryRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeliveryRequestRepository extends JpaRepository<DeliveryRequest, Integer> {
    Optional<DeliveryRequest> findByOrder_Id(Integer orderId);
}