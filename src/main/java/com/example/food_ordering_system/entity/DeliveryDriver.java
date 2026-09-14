package com.example.food_ordering_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "delivery_drivers")
public class DeliveryDriver {
    @Id
    @Column(name = "driver_id", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "driver_id", nullable = false)
    private User users;

    @Column(name = "license_info", nullable = false, length = 100)
    private String licenseInfo;

    @Column(name = "current_location")
    private String currentLocation;

    @ColumnDefault("'pending'")
    @Lob
    @Column(name = "approval_status")
    private String approvalStatus;


}