package com.example.food_ordering_system.repository;

import com.example.food_ordering_system.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Integer> {
    List<SupportTicket> findByUser_IdOrderByCreatedAtDesc(Integer userId);
}