package com.example.food_ordering_system.repository;

import com.example.food_ordering_system.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Integer> {

    // Useful for showing the order timeline to the customer later
    List<OrderStatusHistory> findByOrder_IdOrderByChangedAtAsc(Integer orderId);

}