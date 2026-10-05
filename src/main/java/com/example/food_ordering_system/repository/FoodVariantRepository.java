package com.example.food_ordering_system.repository;

import com.example.food_ordering_system.entity.FoodVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FoodVariantRepository extends JpaRepository<FoodVariant, Integer> {
    List<FoodVariant> findByFoodItem_Id(Integer foodItemId);
}