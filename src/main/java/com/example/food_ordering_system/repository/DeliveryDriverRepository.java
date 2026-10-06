package com.example.food_ordering_system.repository;

import com.example.food_ordering_system.entity.DeliveryDriver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeliveryDriverRepository extends JpaRepository<DeliveryDriver, Integer> {
    List<DeliveryDriver> findByApprovalStatus(String approvalStatus);

    List<DeliveryDriver> findByApprovalStatusAndIsOnline(String approvalStatus, Boolean isOnline);
}