package com.example.food_ordering_system.repository;

import com.example.food_ordering_system.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {
    // JpaRepository gives us save(), findAll(), findById(), and deleteById() automatically

    // Finds approved restaurants matching the search query
    List<Restaurant> findByNameContainingIgnoreCaseAndApprovalStatus(String name, String approvalStatus);

    // Finds all approved restaurants (for the home page "Restaurants near you" stripe)
    List<Restaurant> findByApprovalStatus(String approvalStatus);

    List<Restaurant> findByNameContainingIgnoreCase(String name);



}