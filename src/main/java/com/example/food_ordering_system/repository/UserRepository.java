package com.example.food_ordering_system.repository;

import com.example.food_ordering_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // Spring Data JPA magically writes the SQL for this just based on the method name!
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}