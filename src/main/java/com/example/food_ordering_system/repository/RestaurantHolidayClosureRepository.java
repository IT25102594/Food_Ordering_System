package com.example.food_ordering_system.repository;

import com.example.food_ordering_system.entity.RestaurantHolidayClosure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RestaurantHolidayClosureRepository extends JpaRepository<RestaurantHolidayClosure, Integer> {

    // Get all closures for a specific restaurant, sorted by date
    List<RestaurantHolidayClosure> findByRestaurant_IdOrderByExceptionDateAsc(Integer restaurantId);

    // Helpful to prevent adding two different rules for the exact same day
    boolean existsByRestaurant_IdAndExceptionDate(Integer restaurantId, LocalDate exceptionDate);
}