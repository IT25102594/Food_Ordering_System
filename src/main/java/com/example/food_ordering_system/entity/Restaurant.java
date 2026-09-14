package com.example.food_ordering_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "restaurants")
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "street")
    private String street;

    @Column(name = "city", length = 100)
    private String city;

    @ColumnDefault("0.00")
    @Column(name = "avg_rating", precision = 3, scale = 2)
    private BigDecimal avgRating;

    @ColumnDefault("'pending'")
    @Lob
    @Column(name = "approval_status")
    private String approvalStatus;


}