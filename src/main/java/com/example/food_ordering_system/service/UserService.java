package com.example.food_ordering_system.service;

import com.example.food_ordering_system.dto.UserRegistrationDto;
import com.example.food_ordering_system.dto.UserResponseDto;
import com.example.food_ordering_system.dto.UserEmploymentDto;
import com.example.food_ordering_system.entity.User;
import com.example.food_ordering_system.repository.UserRepository;
import com.example.food_ordering_system.repository.AssignedRoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AssignedRoleRepository assignedRoleRepository;

    public UserService(UserRepository userRepository, AssignedRoleRepository assignedRoleRepository) {
        this.userRepository = userRepository;
        this.assignedRoleRepository = assignedRoleRepository;
    }

    // CREATE (With Email Check!)
    public String registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            return "ERROR_EMAIL_TAKEN";
        }

        User newUser = new User();
        newUser.setFirstName(dto.getFirstName());
        newUser.setLastName(dto.getLastName());
        newUser.setEmail(dto.getEmail());
        newUser.setPasswordHash(dto.getPassword()); // Will encrypt later

        userRepository.save(newUser);
        return "SUCCESS";
    }

    // READ (All Users)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    // READ (Single User by ID)
    public UserResponseDto getUserById(Integer id) {
        Optional<User> userOptional = userRepository.findById(id);
        return userOptional.map(this::mapToResponseDto).orElse(null);
    }

    // UPDATE
    public boolean updateUser(Integer id, UserRegistrationDto dto) {
        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            existingUser.setFirstName(dto.getFirstName());
            existingUser.setLastName(dto.getLastName());
            // Keeping it simple: Not updating email/password in this basic update
            userRepository.save(existingUser);
            return true;
        }
        return false;
    }

    // DELETE
    public boolean deleteUser(Integer id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Helper method to convert Entity to safe DTO
    private UserResponseDto mapToResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setUserId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        return dto;
    }

    // Get all restaurants a user works at (or owns)
    public List<UserEmploymentDto> getUserEmployments(Integer userId) {
        return assignedRoleRepository.findByUser_Id(userId).stream().map(role -> {
            UserEmploymentDto dto = new UserEmploymentDto();
            dto.setRestaurantId(role.getRestaurant().getId());
            dto.setRestaurantName(role.getRestaurant().getName());
            dto.setRoleType(role.getId().getRoleType());
            return dto;
        }).collect(Collectors.toList());
    }
}