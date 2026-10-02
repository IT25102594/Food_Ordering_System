package com.example.food_ordering_system.repository;

import com.example.food_ordering_system.entity.RestaurantReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantReviewRepository extends JpaRepository<RestaurantReview, Long> {//save() and deleteById()
    List<RestaurantReview> findByRestaurantId(Long restaurantId);
}
