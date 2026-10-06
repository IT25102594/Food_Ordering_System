
package com.example.food_ordering_system.controller;

import com.example.food_ordering_system.dto.DeliveryDriverRequestDto;
import com.example.food_ordering_system.entity.DeliveryDriver;
import com.example.food_ordering_system.service.DeliveryDriverService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/drivers")
public class DeliveryDriverController {

    private final DeliveryDriverService driverService;

    public DeliveryDriverController(DeliveryDriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody DeliveryDriverRequestDto dto) {
        String result = driverService.registerDriver(dto);
        if (result.startsWith("Error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    // We fetch by userId since driver_id and user_id are the same (@MapsId)
    @GetMapping("/{userId}")
    public ResponseEntity<?> getDriverStatus(@PathVariable Integer userId) {
        DeliveryDriver driver = driverService.getDriverDetails(userId);
        if (driver == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not registered");
        }

        // We use Math.of() to avoid returning nulls directly which might break the Map
        return ResponseEntity.ok(Map.of(
                "id", driver.getId(),
                "licenseInfo", driver.getLicenseInfo(),
                "approvalStatus", driver.getApprovalStatus(),
                "isOnline", driver.getIsOnline() != null && driver.getIsOnline(),
                "latitude", driver.getLatitude() != null ? driver.getLatitude() : "",
                "longitude", driver.getLongitude() != null ? driver.getLongitude() : ""
        ));
    }

    @PutMapping("/{id}/online")
    public ResponseEntity<String> setOnline(@PathVariable Integer id, @RequestParam boolean isOnline) {
        String result = driverService.setOnline(id, isOnline);
        return result.startsWith("Error") ? ResponseEntity.badRequest().body(result) : ResponseEntity.ok(result);
    }

    // the rider page posts its GPS position here every few seconds while online
    @PutMapping("/{id}/location")
    public ResponseEntity<Void> setLocation(@PathVariable Integer id, @RequestParam BigDecimal lat, @RequestParam BigDecimal lng) {
        driverService.updateLocation(id, lat, lng);
        return ResponseEntity.ok().build();
    }


}