package com.example.food_ordering_system.service;

import com.example.food_ordering_system.dto.RestaurantHolidayClosureRequestDto;
import com.example.food_ordering_system.entity.AssignedRoleId;
import com.example.food_ordering_system.entity.Restaurant;
import com.example.food_ordering_system.entity.RestaurantHolidayClosure;
import com.example.food_ordering_system.repository.AssignedRoleRepository;
import com.example.food_ordering_system.repository.RestaurantHolidayClosureRepository;
import com.example.food_ordering_system.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantHolidayClosureService {

    private final RestaurantHolidayClosureRepository closureRepository;
    private final RestaurantRepository restaurantRepository;
    private final AssignedRoleRepository assignedRoleRepository;

    public RestaurantHolidayClosureService(RestaurantHolidayClosureRepository closureRepository,
                                           RestaurantRepository restaurantRepository,
                                           AssignedRoleRepository assignedRoleRepository) {
        this.closureRepository = closureRepository;
        this.restaurantRepository = restaurantRepository;
        this.assignedRoleRepository = assignedRoleRepository;
    }

    public String createClosure(Integer requesterId, RestaurantHolidayClosureRequestDto dto) {
        if (!hasAtLeastAdminAccess(requesterId, dto.getRestaurantId())) {
            return "Error: Unauthorized. Only Owners and Admins can manage the schedule.";
        }

        if (dto.getExceptionDate() == null) {
            return "Error: A valid date is required.";
        }

        if (closureRepository.existsByRestaurant_IdAndExceptionDate(dto.getRestaurantId(), dto.getExceptionDate())) {
            return "Error: An exception for this specific date already exists.";
        }

        // Validate custom times if it's not fully closed
        if (!dto.getIsFullyClosed()) {
            if (dto.getCustomOpenTime() == null || dto.getCustomCloseTime() == null) {
                return "Error: Custom open and close times must be provided if the restaurant is not fully closed.";
            }
            if (!dto.getCustomOpenTime().isBefore(dto.getCustomCloseTime())) {
                return "Error: Open time must be before the close time.";
            }
        }

        Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId()).orElseThrow();

        RestaurantHolidayClosure closure = new RestaurantHolidayClosure();
        closure.setRestaurant(restaurant);
        closure.setExceptionDate(dto.getExceptionDate());
        closure.setReason(dto.getReason());
        closure.setIsFullyClosed(dto.getIsFullyClosed());

        if (dto.getIsFullyClosed()) {
            closure.setCustomOpenTime(null);
            closure.setCustomCloseTime(null);
        } else {
            closure.setCustomOpenTime(dto.getCustomOpenTime());
            closure.setCustomCloseTime(dto.getCustomCloseTime());
        }

        closureRepository.save(closure);
        return "Success: Schedule exception saved.";
    }

    public List<RestaurantHolidayClosure> getClosuresForRestaurant(Integer restaurantId) {
        return closureRepository.findByRestaurant_IdOrderByExceptionDateAsc(restaurantId);
    }

    public String deleteClosure(Integer closureId, Integer requesterId, Integer restaurantId) {
        if (!hasAtLeastAdminAccess(requesterId, restaurantId)) {
            return "Error: Unauthorized.";
        }

        if (!closureRepository.existsById(closureId)) {
            return "Error: Schedule exception not found.";
        }

        closureRepository.deleteById(closureId);
        return "Success: Schedule exception removed successfully.";
    }

    private boolean hasAtLeastAdminAccess(Integer userId, Integer restaurantId) {
        return assignedRoleRepository.existsById(new AssignedRoleId(userId, restaurantId, "RestaurantOwner")) ||
                assignedRoleRepository.existsById(new AssignedRoleId(userId, restaurantId, "RestaurantAdmin"));
    }
}