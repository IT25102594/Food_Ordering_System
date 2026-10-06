package com.example.food_ordering_system.repository;

import com.example.food_ordering_system.entity.CustomerSupportAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerSupportAgentRepository extends JpaRepository<CustomerSupportAgent, Integer> {
}