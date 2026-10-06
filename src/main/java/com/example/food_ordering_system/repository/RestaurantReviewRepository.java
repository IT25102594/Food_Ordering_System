package com.example.food_ordering_system.repository;

import com.example.food_ordering_system.entity.RestaurantReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RestaurantReviewRepository extends JpaRepository<RestaurantReview, Integer> {
    Optional<RestaurantReview> findByUser_IdAndRestaurant_Id(Integer userId, Integer restaurantId);
}