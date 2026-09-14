package com.example.food_ordering_system.repository;

import com.example.food_ordering_system.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {
    // JpaRepository gives us save(), findAll(), findById(), and deleteById() automatically!
}