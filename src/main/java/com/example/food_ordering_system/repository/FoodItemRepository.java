package com.example.food_ordering_system.repository;

import com.example.food_ordering_system.entity.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FoodItemRepository extends JpaRepository<FoodItem, Integer> {
    List<FoodItem> findByRestaurant_Id(Integer restaurantId);
    List<FoodItem> findByCategory_Id(Integer categoryId);


    List<FoodItem> findByCategory_NameIgnoreCase(String categoryName);
    List<FoodItem> findByNameContainingIgnoreCase(String name);

}