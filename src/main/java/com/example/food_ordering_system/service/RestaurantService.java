package com.example.food_ordering_system.service;

import com.example.food_ordering_system.dto.RestaurantRequestDto;
import com.example.food_ordering_system.dto.RestaurantResponseDto;
import com.example.food_ordering_system.dto.StaffMemberDto;
import com.example.food_ordering_system.entity.AssignedRole;
import com.example.food_ordering_system.entity.AssignedRoleId;
import com.example.food_ordering_system.entity.Restaurant;
import com.example.food_ordering_system.entity.User;
import com.example.food_ordering_system.repository.AssignedRoleRepository;
import com.example.food_ordering_system.repository.RestaurantRepository;
import com.example.food_ordering_system.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final AssignedRoleRepository assignedRoleRepository;

    public RestaurantService(RestaurantRepository restaurantRepository,
                             UserRepository userRepository,
                             AssignedRoleRepository assignedRoleRepository) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.assignedRoleRepository = assignedRoleRepository;
    }

    // CREATE (Now with Owner Linking!)
    public String createRestaurant(RestaurantRequestDto dto) {
        // 1. Verify the User exists first
        Optional<User> optionalUser = userRepository.findById(dto.getUserId());
        if (optionalUser.isEmpty()) {
            return "Error: User not found!";
        }
        User owner = optionalUser.get();

        // 2. Create and Save the Restaurant
        Restaurant restaurant = new Restaurant();
        restaurant.setName(dto.getName());
        restaurant.setStreet(dto.getStreet());
        restaurant.setCity(dto.getCity());
        restaurant.setAvgRating(BigDecimal.ZERO);
        restaurant.setApprovalStatus("pending");

        // We save it first so MySQL generates the new Restaurant ID
        restaurant = restaurantRepository.save(restaurant);

        // 3. Create the Composite Key for the Role
        AssignedRoleId roleId = new AssignedRoleId();
        roleId.setUserId(owner.getId());
        roleId.setRestaurantId(restaurant.getId());
        roleId.setRoleType("RestaurantOwner"); // Must match your DB Enum!

        // 4. Create and Save the Assigned Role Junction
        AssignedRole assignedRole = new AssignedRole();
        assignedRole.setId(roleId);
        assignedRole.setUser(owner);
        assignedRole.setRestaurant(restaurant);

        assignedRoleRepository.save(assignedRole);

        return "Success: Restaurant created and Owner assigned!";
    }

    // READ ALL
    public List<RestaurantResponseDto> getAllRestaurants() {
        return restaurantRepository.findAll().stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    // READ SINGLE
    public RestaurantResponseDto getRestaurantById(Integer id) {
        Optional<Restaurant> optionalRestaurant = restaurantRepository.findById(id);
        return optionalRestaurant.map(this::mapToResponseDto).orElse(null);
    }

    // UPDATE
    public boolean updateRestaurant(Integer id, RestaurantRequestDto dto) {
        Optional<Restaurant> optionalRestaurant = restaurantRepository.findById(id);
        if (optionalRestaurant.isPresent()) {
            Restaurant existingRestaurant = optionalRestaurant.get();
            existingRestaurant.setName(dto.getName());
            existingRestaurant.setStreet(dto.getStreet());
            existingRestaurant.setCity(dto.getCity());
            restaurantRepository.save(existingRestaurant);
            return true;
        }
        return false;
    }

    // DELETE
    public boolean deleteRestaurant(Integer id) {
        if (restaurantRepository.existsById(id)) {
            restaurantRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Helper method
    private RestaurantResponseDto mapToResponseDto(Restaurant restaurant) {
        RestaurantResponseDto dto = new RestaurantResponseDto();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setStreet(restaurant.getStreet());
        dto.setCity(restaurant.getCity());
        dto.setAvgRating(restaurant.getAvgRating());
        dto.setApprovalStatus(restaurant.getApprovalStatus());
        return dto;
    }
    // Get all staff for a restaurant
    public List<StaffMemberDto> getRestaurantStaff(Integer restaurantId) {
        return assignedRoleRepository.findByRestaurant_Id(restaurantId).stream().map(role -> {
            StaffMemberDto dto = new StaffMemberDto();
            dto.setUserId(role.getUser().getId());
            dto.setFirstName(role.getUser().getFirstName());
            dto.setLastName(role.getUser().getLastName());
            dto.setRoleType(role.getId().getRoleType());
            return dto;
        }).collect(Collectors.toList());
    }


}