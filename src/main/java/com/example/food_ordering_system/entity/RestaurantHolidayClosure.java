package com.example.food_ordering_system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "restaurant_holiday_closures")
public class RestaurantHolidayClosure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "closure_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @NotNull
    @Column(name = "exception_date", nullable = false)
    private LocalDate exceptionDate;

    @Size(max = 100)
    @Column(name = "reason", length = 100)
    private String reason;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "is_fully_closed", nullable = false)
    private Boolean isFullyClosed;

    @Column(name = "custom_open_time")
    private LocalTime customOpenTime;

    @Column(name = "custom_close_time")
    private LocalTime customCloseTime;


}