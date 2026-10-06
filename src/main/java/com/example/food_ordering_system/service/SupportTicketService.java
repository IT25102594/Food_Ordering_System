package com.example.food_ordering_system.service;

import com.example.food_ordering_system.dto.SupportTicketDto;
import com.example.food_ordering_system.entity.SupportTicket;
import com.example.food_ordering_system.repository.SupportTicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;

    public SupportTicketService(SupportTicketRepository supportTicketRepository) {
        this.supportTicketRepository = supportTicketRepository;
    }

    public SupportTicket createTicket(SupportTicketDto dto) {
        SupportTicket ticket = new SupportTicket(
                dto.getUserId(),
                dto.getSubject(),
                dto.getDescription(),
                dto.getStatus() != null ? dto.getStatus() : "OPEN"
        );
        return supportTicketRepository.save(ticket);
    }

    public List<SupportTicket> getAllTickets() {
        return supportTicketRepository.findAll();
    }
}