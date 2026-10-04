package com.example.food_ordering_system.service;

import com.example.food_ordering_system.dto.RestaurantStaffRequestDto;
import com.example.food_ordering_system.dto.RestaurantStaffResponseDto;
import com.example.food_ordering_system.entity.AssignedRole;
import com.example.food_ordering_system.entity.AssignedRoleId;
import com.example.food_ordering_system.entity.Restaurant;
import com.example.food_ordering_system.entity.User;
import com.example.food_ordering_system.repository.AssignedRoleRepository;
import com.example.food_ordering_system.repository.RestaurantRepository;
import com.example.food_ordering_system.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RestaurantStaffService {

    private final AssignedRoleRepository assignedRoleRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    public RestaurantStaffService(AssignedRoleRepository assignedRoleRepository,
                                  UserRepository userRepository,
                                  RestaurantRepository restaurantRepository) {
        this.assignedRoleRepository = assignedRoleRepository;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public List<RestaurantStaffResponseDto> getRestaurantStaff(Integer restaurantId, Integer requesterId) {
        if (!hasAtLeastAdminAccess(requesterId, restaurantId)) {
            throw new RuntimeException("Error: Unauthorized. Only Owners and Admins can view staff.");
        }

        List<AssignedRole> roles = assignedRoleRepository.findById_RestaurantId(restaurantId);

        return roles.stream().map(role -> {
            RestaurantStaffResponseDto dto = new RestaurantStaffResponseDto();
            dto.setUserId(role.getUser().getId());
            dto.setName(role.getUser().getFirstName() + " " + role.getUser().getLastName());
            dto.setEmail(role.getUser().getEmail());
            dto.setRoleType(role.getId().getRoleType());
            dto.setAssignedAt(role.getAssignedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    public String hireRestaurantStaff(Integer restaurantId, Integer requesterId, RestaurantStaffRequestDto dto) {
        if (dto.getRoleType().equals("RestaurantOwner")) {
            return "Error: Cannot assign Owner role through the staff portal.";
        }

        boolean isRequesterOwner = assignedRoleRepository.existsById(new AssignedRoleId(requesterId, restaurantId, "RestaurantOwner"));
        boolean isRequesterAdmin = assignedRoleRepository.existsById(new AssignedRoleId(requesterId, restaurantId, "RestaurantAdmin"));

        if (!isRequesterOwner && !isRequesterAdmin) {
            return "Error: Unauthorized.";
        }

        if (dto.getRoleType().equals("RestaurantAdmin") && !isRequesterOwner) {
            return "Error: Only the Restaurant Owner can hire an Admin.";
        }

        Optional<User> userOpt = userRepository.findByEmail(dto.getEmail());
        if (userOpt.isEmpty()) {
            return "Error: No user found with email " + dto.getEmail();
        }
        User targetUser = userOpt.get();

        Optional<Restaurant> restOpt = restaurantRepository.findById(restaurantId);
        if (restOpt.isEmpty()) return "Error: Restaurant not found.";

        AssignedRoleId newRoleId = new AssignedRoleId();
        newRoleId.setUserId(targetUser.getId());
        newRoleId.setRestaurantId(restaurantId);
        newRoleId.setRoleType(dto.getRoleType());

        if (assignedRoleRepository.existsById(newRoleId)) {
            return "Error: User is already hired for this role.";
        }

        AssignedRole newRole = new AssignedRole();
        newRole.setId(newRoleId);
        newRole.setUser(targetUser);
        newRole.setRestaurant(restOpt.get());
        newRole.setAssignedAt(Instant.now());
        newRole.setIsOnline(false); // Default to offline when hired

        assignedRoleRepository.save(newRole);

        return "Success: " + targetUser.getFirstName() + " " + targetUser.getLastName() + " hired as " + dto.getRoleType();
    }

    public List<java.util.Map<String, String>> searchUsersForHiring(String query) {
        return userRepository.findByEmailContainingIgnoreCase(query).stream()
                .map(u -> java.util.Map.of(
                        "name", u.getFirstName() + " " + u.getLastName(),
                        "email", u.getEmail()
                ))
                .limit(5)
                .collect(Collectors.toList());
    }

    public String fireRestaurantStaff(Integer restaurantId, Integer requesterId, Integer targetUserId, String targetRoleType) {
        if (targetRoleType.equals("RestaurantOwner")) {
            return "Error: The Restaurant Owner cannot be fired.";
        }
        if (requesterId.equals(targetUserId)) {
            return "Error: You cannot fire yourself.";
        }

        boolean isRequesterOwner = assignedRoleRepository.existsById(new AssignedRoleId(requesterId, restaurantId, "RestaurantOwner"));

        if (targetRoleType.equals("RestaurantAdmin") && !isRequesterOwner) {
            return "Error: Only the Restaurant Owner can fire an Admin.";
        }

        if (!isRequesterOwner && !assignedRoleRepository.existsById(new AssignedRoleId(requesterId, restaurantId, "RestaurantAdmin"))) {
            return "Error: Unauthorized.";
        }

        AssignedRoleId targetRoleId = new AssignedRoleId();
        targetRoleId.setUserId(targetUserId);
        targetRoleId.setRestaurantId(restaurantId);
        targetRoleId.setRoleType(targetRoleType);

        if (!assignedRoleRepository.existsById(targetRoleId)) {
            return "Error: Staff member not found in this role.";
        }

        assignedRoleRepository.deleteById(targetRoleId);
        return "Success: Staff member removed from " + targetRoleType;
    }

    private boolean hasAtLeastAdminAccess(Integer userId, Integer restaurantId) {
        return assignedRoleRepository.existsById(new AssignedRoleId(userId, restaurantId, "RestaurantOwner")) ||
                assignedRoleRepository.existsById(new AssignedRoleId(userId, restaurantId, "RestaurantAdmin"));
    }

    // --- NEW ONLINE METHODS ---

    public long getOnlineStaffCount(Integer restaurantId) {
        return assignedRoleRepository.countOnlineStaffByRestaurantId(restaurantId);
    }

    public boolean checkUserOnlineStatus(Integer userId, Integer restaurantId) {
        return assignedRoleRepository.checkUserOnlineStatusAtRestaurant(userId, restaurantId);
    }

    @Transactional
    public String toggleOnlineStatus(Integer userId, Integer restaurantId, boolean isOnline) {

        // If turning ON, log them out everywhere else first to prevent multi-workplace concurrency
        if (isOnline) {
            assignedRoleRepository.setAllRolesOfflineForUser(userId);
        }

        int updatedRows = assignedRoleRepository.updateOnlineStatus(userId, restaurantId, isOnline);

        if (updatedRows > 0) {
            return "Success: Status updated to " + (isOnline ? "Online" : "Offline");
        }
        return "Error: Staff record not found for this restaurant.";
    }

}