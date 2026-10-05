package com.example.food_ordering_system.service;

import com.example.food_ordering_system.dto.*;
import com.example.food_ordering_system.entity.*;
import com.example.food_ordering_system.repository.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final SystemAdminRepository adminRepo;
    private final AssignedRoleRepository assignedRoleRepository;
    private final UserRepository userRepository;
    private final RestaurantWorkingHourRepository workingHourRepository; // newly added

    public RestaurantService(RestaurantRepository restaurantRepository,
                             SystemAdminRepository adminRepo,
                             AssignedRoleRepository assignedRoleRepository,
                             UserRepository userRepository,
                             RestaurantWorkingHourRepository workingHourRepository) {
        this.restaurantRepository = restaurantRepository;
        this.adminRepo = adminRepo;
        this.assignedRoleRepository = assignedRoleRepository;
        this.userRepository = userRepository;
        this.workingHourRepository = workingHourRepository;
    }

    // --- Standard CRUD ---

    public String createRestaurant(RestaurantRequestDto dto) {
        Optional<User> ownerOpt = userRepository.findById(dto.getUserId());
        if (ownerOpt.isEmpty()) {
            return "Error: User not found. Cannot assign owner.";
        }
        User owner = ownerOpt.get();

        Restaurant restaurant = new Restaurant();
        restaurant.setName(dto.getName());
        restaurant.setAddressLine(dto.getAddressLine());
        restaurant.setCity(dto.getCity());
        restaurant.setLogoUrl(dto.getLogoUrl());
        restaurant.setBannerUrl(dto.getBannerUrl());
        restaurant.setLatitude(dto.getLatitude());
        restaurant.setLongitude(dto.getLongitude());
        // set the manual open/close toggle
        restaurant.setIsOpen(dto.getIsOpen() != null ? dto.getIsOpen() : false);
        restaurant.setApprovalStatus("pending");
        restaurant.setAvgRating(java.math.BigDecimal.ZERO);

        restaurant = restaurantRepository.save(restaurant);

        // loop and save all 7 days of working hours in one go
        if (dto.getWorkingHours() != null && !dto.getWorkingHours().isEmpty()) {
            for (WorkingHourDto hourDto : dto.getWorkingHours()) {
                RestaurantWorkingHour rh = new RestaurantWorkingHour();
                rh.setRestaurant(restaurant);
                rh.setDayOfWeek(hourDto.getDayOfWeek());
                rh.setOpenTime(hourDto.getOpenTime());
                rh.setCloseTime(hourDto.getCloseTime());
                rh.setIsClosed(hourDto.getIsClosed() != null ? hourDto.getIsClosed() : false);
                workingHourRepository.save(rh);
            }
        }

        AssignedRoleId roleId = new AssignedRoleId();
        roleId.setUserId(owner.getId());
        roleId.setRestaurantId(restaurant.getId());
        roleId.setRoleType("RestaurantOwner");

        AssignedRole assignedRole = new AssignedRole();
        assignedRole.setId(roleId);
        assignedRole.setUser(owner);
        assignedRole.setRestaurant(restaurant);
        assignedRole.setAssignedAt(Instant.now());

        assignedRoleRepository.save(assignedRole);

        return "Success: Restaurant created and Owner assigned!";
    }

    public List<RestaurantResponseDto> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public RestaurantResponseDto getRestaurantById(Integer id) {
        Optional<Restaurant> restOpt = restaurantRepository.findById(id);
        return restOpt.map(this::mapToResponseDto).orElse(null);
    }

    public String updateRestaurant(Integer id, Integer requesterId, RestaurantRequestDto dto) {
        // Enforce RBAC for Settings & Schedule
        boolean isOwner = assignedRoleRepository.existsById(new AssignedRoleId(requesterId, id, "RestaurantOwner"));
        boolean isAdmin = assignedRoleRepository.existsById(new AssignedRoleId(requesterId, id, "RestaurantAdmin"));

        if (!isOwner && !isAdmin) {
            return "Error: Unauthorized. Only Owners and Admins can update restaurant settings.";
        }

        Optional<Restaurant> restOpt = restaurantRepository.findById(id);
        if (restOpt.isPresent()) {
            Restaurant restaurant = restOpt.get();
            if (dto.getName() != null) restaurant.setName(dto.getName());
            if (dto.getAddressLine() != null) restaurant.setAddressLine(dto.getAddressLine());
            if (dto.getCity() != null) restaurant.setCity(dto.getCity());
            if (dto.getLogoUrl() != null) restaurant.setLogoUrl(dto.getLogoUrl());
            if (dto.getBannerUrl() != null) restaurant.setBannerUrl(dto.getBannerUrl());
            if (dto.getIsOpen() != null) restaurant.setIsOpen(dto.getIsOpen());
            if (dto.getLatitude() != null) restaurant.setLatitude(dto.getLatitude());
            if (dto.getLongitude() != null) restaurant.setLongitude(dto.getLongitude());
            restaurantRepository.save(restaurant);

            if (dto.getWorkingHours() != null) {
                List<RestaurantWorkingHour> existingHours = workingHourRepository.findByRestaurant_Id(id);
                workingHourRepository.deleteAll(existingHours);

                for (WorkingHourDto hourDto : dto.getWorkingHours()) {
                    RestaurantWorkingHour rh = new RestaurantWorkingHour();
                    rh.setRestaurant(restaurant);
                    rh.setDayOfWeek(hourDto.getDayOfWeek());
                    rh.setOpenTime(hourDto.getOpenTime());
                    rh.setCloseTime(hourDto.getCloseTime());
                    rh.setIsClosed(hourDto.getIsClosed() != null ? hourDto.getIsClosed() : false);
                    workingHourRepository.save(rh);
                }
            }
            return "Success: Restaurant updated.";
        }
        return "Error: Restaurant not found.";
    }

    public boolean deleteRestaurant(Integer id) {
        if (restaurantRepository.existsById(id)) {
            restaurantRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // --- Staff Management --- (same as before)

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

    public String assignStaffToRestaurant(Integer restaurantId, Integer requesterId, UserEmploymentDto dto) {
        AssignedRoleId requesterRoleId = new AssignedRoleId();
        requesterRoleId.setUserId(requesterId);
        requesterRoleId.setRestaurantId(restaurantId);

        AssignedRoleId ownerCheck = new AssignedRoleId();
        ownerCheck.setUserId(requesterId);
        ownerCheck.setRestaurantId(restaurantId);
        ownerCheck.setRoleType("RestaurantOwner");
        boolean isOwner = assignedRoleRepository.existsById(ownerCheck);

        AssignedRoleId adminCheck = new AssignedRoleId();
        adminCheck.setUserId(requesterId);
        adminCheck.setRestaurantId(restaurantId);
        adminCheck.setRoleType("RestaurantAdmin");
        boolean isAdmin = assignedRoleRepository.existsById(adminCheck);

        if (!isOwner && !isAdmin) {
            return "Error: Unauthorized. You are not staff at this restaurant.";
        }

        String roleToAssign = dto.getRoleType();

        if (roleToAssign.equals("RestaurantOwner")) {
            return "Error: Only the system can create an Owner when a restaurant is registered.";
        }

        if (roleToAssign.equals("RestaurantAdmin") && !isOwner) {
            return "Error: Unauthorized. Only a Restaurant Owner can hire an Admin.";
        }

        Optional<Restaurant> restOpt = restaurantRepository.findById(restaurantId);
        Optional<User> userOpt = userRepository.findById(dto.getUserId());

        if (restOpt.isEmpty()) return "Error: Restaurant not found.";
        if (userOpt.isEmpty()) return "Error: Target user not found.";

        AssignedRoleId roleId = new AssignedRoleId();
        roleId.setUserId(dto.getUserId());
        roleId.setRestaurantId(restaurantId);
        roleId.setRoleType(roleToAssign);

        AssignedRole assignedRole = new AssignedRole();
        assignedRole.setId(roleId);
        assignedRole.setUser(userOpt.get());
        assignedRole.setRestaurant(restOpt.get());
        assignedRole.setAssignedAt(Instant.now());

        assignedRoleRepository.save(assignedRole);

        return "Success: User " + dto.getUserId() + " assigned as " + roleToAssign + " to restaurant " + restaurantId;
    }

    // --- Admin Actions --- (same as before)

    public String approveRestaurant(Integer restaurantId, Integer adminId) {
        if (!adminRepo.existsById(adminId)) {
            return "Error: Unauthorized. Only admins can approve restaurants.";
        }

        Optional<Restaurant> restOpt = restaurantRepository.findById(restaurantId);
        if (restOpt.isEmpty()) {
            return "Error: Restaurant not found.";
        }

        Restaurant restaurant = restOpt.get();
        restaurant.setApprovalStatus("approved");
        restaurantRepository.save(restaurant);

        return "Success: Restaurant has been officially approved!";
    }

    // --- Helpers ---

    private RestaurantResponseDto mapToResponseDto(Restaurant restaurant) {
        RestaurantResponseDto dto = new RestaurantResponseDto();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setAddressLine(restaurant.getAddressLine());
        dto.setCity(restaurant.getCity());
        dto.setLogoUrl(restaurant.getLogoUrl());
        dto.setBannerUrl(restaurant.getBannerUrl());
        dto.setAvgRating(restaurant.getAvgRating());
        dto.setApprovalStatus(restaurant.getApprovalStatus());
        dto.setIsOpen(restaurant.getIsOpen());
        dto.setLatitude(restaurant.getLatitude());
        dto.setLongitude(restaurant.getLongitude());

        // map the hours so they show up in the frontend response
        List<WorkingHourDto> hours = workingHourRepository.findByRestaurant_Id(restaurant.getId()).stream()
                .map(rh -> {
                    WorkingHourDto hDto = new WorkingHourDto();
                    hDto.setDayOfWeek(rh.getDayOfWeek());
                    hDto.setOpenTime(rh.getOpenTime());
                    hDto.setCloseTime(rh.getCloseTime());
                    hDto.setIsClosed(rh.getIsClosed());
                    return hDto;
                }).collect(Collectors.toList());
        dto.setWorkingHours(hours);

        return dto;
    }
}