package com.example.food_ordering_system.repository;

import com.example.food_ordering_system.entity.AssignedRole;
import com.example.food_ordering_system.entity.AssignedRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignedRoleRepository extends JpaRepository<AssignedRole, AssignedRoleId> {

    // Finds all staff for a specific restaurant
    List<AssignedRole> findByRestaurant_Id(Integer restaurantId);

    // Finds all jobs/roles for a specific user
    List<AssignedRole> findByUser_Id(Integer userId);
}