package com.example.food_ordering_system.repository;

import com.example.food_ordering_system.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
//comment
@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> { //save() and deleteById()
    List<SupportTicket> findByUserId(Long userId);
}