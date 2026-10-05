package com.example.food_ordering_system.repository;

import com.example.food_ordering_system.entity.RestaurantWorkingHour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantWorkingHourRepository extends JpaRepository<RestaurantWorkingHour, Integer> {
    List<RestaurantWorkingHour> findByRestaurant_Id(Integer restaurantId);
}