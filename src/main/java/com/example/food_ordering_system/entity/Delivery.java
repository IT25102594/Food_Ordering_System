package com.example.food_ordering_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;



    @Entity
    @Table(name = "deliveries")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Delivery {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String orderId;
        private String riderId;
        private String riderName;
        private String customerAddress;
        private String status; // ASSIGNED, ON_THE_WAY, DELIVERED
        private Double currentLatitude;
        private Double currentLongitude;
        private LocalDateTime updatedAt;
    }

