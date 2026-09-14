package com.example.food_ordering_system.service;

import com.example.food_ordering_system.dto.AdminResponseDto;
import com.example.food_ordering_system.entity.Restaurant;
import com.example.food_ordering_system.entity.SystemAdmin;
import com.example.food_ordering_system.entity.User;
import com.example.food_ordering_system.repository.RestaurantRepository;
import com.example.food_ordering_system.repository.SystemAdminRepository;
import com.example.food_ordering_system.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AdminService {

    private final SystemAdminRepository adminRepo;
    private final UserRepository userRepo;
    private final RestaurantRepository restaurantRepo;

    public AdminService(SystemAdminRepository adminRepo, UserRepository userRepo, RestaurantRepository restaurantRepo) {
        this.adminRepo = adminRepo;
        this.userRepo = userRepo;
        this.restaurantRepo = restaurantRepo;
    }

    // 1. View Admin Details
    public AdminResponseDto getAdminDetails(Integer adminId) {
        Optional<SystemAdmin> adminOpt = adminRepo.findById(adminId);
        if (adminOpt.isPresent()) {
            SystemAdmin admin = adminOpt.get();
            AdminResponseDto dto = new AdminResponseDto();
            dto.setAdminId(admin.getId());
            dto.setFirstName(admin.getUsers().getFirstName()); // Fetches from User table!
            dto.setEmail(admin.getUsers().getEmail());
            dto.setIsSuperAdmin(admin.getIsSuperAdmin());
            return dto;
        }
        return null;
    }

    // 2. Promote to Normal Admin
    public String promoteToNormalAdmin(Integer userId) {
        Optional<User> userOpt = userRepo.findById(userId);
        if (userOpt.isEmpty()) return "Error: User not found.";

        SystemAdmin newAdmin = new SystemAdmin();
        newAdmin.setUsers(userOpt.get());
        newAdmin.setIsSuperAdmin(false); // FALSE makes them a normal admin

        adminRepo.save(newAdmin);
        return "Success: User promoted to Normal Admin!";
    }

    // 3. Approve a Restaurant
    public String approveRestaurant(Integer restaurantId, Integer adminId) {
        // First, verify the user making the request is actually an admin
        if (!adminRepo.existsById(adminId)) {
            return "Error: Unauthorized. Only admins can approve restaurants.";
        }

        // Fetch the restaurant
        Optional<Restaurant> restOpt = restaurantRepo.findById(restaurantId);
        if (restOpt.isEmpty()) {
            return "Error: Restaurant not found.";
        }

        // Update the status
        Restaurant restaurant = restOpt.get();
        restaurant.setApprovalStatus("approved"); // Or whatever exact string/enum your DB uses
        restaurantRepo.save(restaurant);

        return "Success: Restaurant has been officially approved!";
    }
}