package com.example.food_ordering_system.service;

import com.example.food_ordering_system.dto.DeliveryDriverRequestDto;
import com.example.food_ordering_system.entity.DeliveryDriver;
import com.example.food_ordering_system.entity.User;
import com.example.food_ordering_system.repository.DeliveryDriverRepository;
import com.example.food_ordering_system.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class DeliveryDriverService {

    private final DeliveryDriverRepository driverRepository;
    private final UserRepository userRepository;

    public DeliveryDriverService(DeliveryDriverRepository driverRepository, UserRepository userRepository) {
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
    }

    public String registerDriver(DeliveryDriverRequestDto dto) {
        if (driverRepository.existsById(dto.getUserId())) {
            return "Error: You have already applied.";
        }

        Optional<User> userOpt = userRepository.findById(dto.getUserId());
        if (userOpt.isEmpty()) {
            return "Error: User not found.";
        }

        DeliveryDriver driver = new DeliveryDriver();
        driver.setUsers(userOpt.get());
        driver.setLicenseInfo(dto.getLicenseInfo());
        driver.setApprovalStatus("pending");

        driverRepository.save(driver);
        return "Success: Driver application submitted.";
    }

    public DeliveryDriver getDriverDetails(Integer userId) {
        return driverRepository.findById(userId).orElse(null);
    }

    public String setOnline(Integer id, boolean online) {
        DeliveryDriver driver = driverRepository.findById(id).orElse(null);
        if (driver == null) return "Error: Driver not found.";
        if (!"approved".equals(driver.getApprovalStatus())) return "Error: Your application is not approved yet.";
        driver.setIsOnline(online);
        driverRepository.save(driver);
        return "Success: You are " + (online ? "online." : "offline.");
    }

    public void updateLocation(Integer id, BigDecimal lat, BigDecimal lng) {
        driverRepository.findById(id).ifPresent(d -> {
            d.setLatitude(lat);
            d.setLongitude(lng);
            driverRepository.save(d);
        });
    }
}