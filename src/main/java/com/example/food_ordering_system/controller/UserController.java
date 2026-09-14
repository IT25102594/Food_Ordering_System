package com.example.food_ordering_system.controller;

import com.example.food_ordering_system.dto.UserEmploymentDto;
import com.example.food_ordering_system.dto.UserRegistrationDto;
import com.example.food_ordering_system.dto.UserResponseDto;
import com.example.food_ordering_system.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 1. CREATE User
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRegistrationDto dto) {
        String result = userService.registerUser(dto);

        if (result.equals("ERROR_EMAIL_TAKEN")) {
            // Returns 400 Bad Request with an error message
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: An account with this email already exists.");
        }

        // Returns 201 Created on success
        return ResponseEntity.status(HttpStatus.CREATED).body("Success: User registered successfully!");
    }

    // 2. READ All Users
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // 3. READ Single User
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Integer id) {
        UserResponseDto user = userService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // 4. UPDATE User
    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Integer id, @RequestBody UserRegistrationDto dto) {
        boolean isUpdated = userService.updateUser(id, dto);
        if (isUpdated) {
            return ResponseEntity.ok("Success: User updated.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: User not found.");
    }

    // 5. DELETE User
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        boolean isDeleted = userService.deleteUser(id);
        if (isDeleted) {
            return ResponseEntity.ok("Success: User deleted.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: User not found.");
    }

    // Get all employments/owned restaurants for a user
    @GetMapping("/{id}/employments")
    public ResponseEntity<List<UserEmploymentDto>> getUserEmployments(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUserEmployments(id));
    }
}