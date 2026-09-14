package com.example.food_ordering_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class AssignedRoleId implements Serializable {
    private static final long serialVersionUID = -8461724675152738392L;
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "restaurant_id", nullable = false)
    private Integer restaurantId;

    @Lob
    @Column(name = "role_type", nullable = false)
    private String roleType;


}