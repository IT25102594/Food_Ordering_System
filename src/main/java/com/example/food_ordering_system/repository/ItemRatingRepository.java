package com.example.food_ordering_system.repository;

import com.example.food_ordering_system.entity.ItemRating;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ItemRatingRepository extends JpaRepository<ItemRating, Integer> {
    Optional<ItemRating> findByUser_IdAndFoodItem_Id(Integer userId, Integer foodItemId);
}