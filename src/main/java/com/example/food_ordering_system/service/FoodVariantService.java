package com.example.food_ordering_system.service;

import com.example.food_ordering_system.dto.FoodVariantRequestDto;
import com.example.food_ordering_system.dto.FoodVariantResponseDto;
import com.example.food_ordering_system.entity.AssignedRoleId;
import com.example.food_ordering_system.entity.FoodItem;
import com.example.food_ordering_system.entity.FoodVariant;
import com.example.food_ordering_system.repository.AssignedRoleRepository;
import com.example.food_ordering_system.repository.FoodItemRepository;
import com.example.food_ordering_system.repository.FoodVariantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FoodVariantService {

    private final FoodVariantRepository foodVariantRepository;
    private final FoodItemRepository foodItemRepository;
    private final AssignedRoleRepository assignedRoleRepository;

    public FoodVariantService(FoodVariantRepository foodVariantRepository,
                              FoodItemRepository foodItemRepository,
                              AssignedRoleRepository assignedRoleRepository) {
        this.foodVariantRepository = foodVariantRepository;
        this.foodItemRepository = foodItemRepository;
        this.assignedRoleRepository = assignedRoleRepository;
    }

    public String addVariant(Integer itemId, Integer requesterId, FoodVariantRequestDto dto) {
        Optional<FoodItem> itemOpt = foodItemRepository.findById(itemId);
        if (itemOpt.isEmpty()) return "Error: Food item not found.";

        FoodItem foodItem = itemOpt.get();
        if (!isAuthorizedToEdit(requesterId, foodItem.getRestaurant().getId())) {
            return "Error: Unauthorized. Only Restaurant Owners or Admins can modify the menu.";
        }

        FoodVariant variant = new FoodVariant();
        variant.setFoodItem(foodItem);
        variant.setVariantName(dto.getVariantName());
        variant.setPrice(dto.getPrice());
        variant.setImageUrl(dto.getImageUrl());
        variant.setStockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : -1);
        variant.setAvailabilityStatus(dto.getAvailabilityStatus() != null ? dto.getAvailabilityStatus() : true);

        foodVariantRepository.save(variant);
        return "Success: Variant '" + variant.getVariantName() + "' added successfully.";
    }

    public String updateVariant(Integer variantId, Integer requesterId, FoodVariantRequestDto dto) {
        Optional<FoodVariant> variantOpt = foodVariantRepository.findById(variantId);
        if (variantOpt.isEmpty()) return "Error: Variant not found.";

        FoodVariant variant = variantOpt.get();
        if (!isAuthorizedToEdit(requesterId, variant.getFoodItem().getRestaurant().getId())) {
            return "Error: Unauthorized. Only Restaurant Owners or Admins can modify the menu.";
        }

        if (dto.getVariantName() != null) variant.setVariantName(dto.getVariantName());
        if (dto.getPrice() != null) variant.setPrice(dto.getPrice());
        if (dto.getImageUrl() != null) variant.setImageUrl(dto.getImageUrl());
        if (dto.getStockQuantity() != null) variant.setStockQuantity(dto.getStockQuantity());
        if (dto.getAvailabilityStatus() != null) variant.setAvailabilityStatus(dto.getAvailabilityStatus());

        foodVariantRepository.save(variant);
        return "Success: Variant details updated successfully.";
    }

    public String deleteVariant(Integer variantId, Integer requesterId) {
        Optional<FoodVariant> variantOpt = foodVariantRepository.findById(variantId);
        if (variantOpt.isEmpty()) return "Error: Variant not found.";

        FoodVariant variant = variantOpt.get();
        if (!isAuthorizedToEdit(requesterId, variant.getFoodItem().getRestaurant().getId())) {
            return "Error: Unauthorized. Only Restaurant Owners or Admins can modify the menu.";
        }

        List<FoodVariant> existingVariants = foodVariantRepository.findByFoodItem_Id(variant.getFoodItem().getId());
        if (existingVariants.size() <= 1) {
            return "Error: Cannot delete the last variant. A food item must have at least one valid price option.";
        }

        foodVariantRepository.delete(variant);
        return "Success: Variant deleted successfully.";
    }

    private boolean isAuthorizedToEdit(Integer requesterId, Integer restaurantId) {
        AssignedRoleId ownerCheck = new AssignedRoleId();
        ownerCheck.setUserId(requesterId);
        ownerCheck.setRestaurantId(restaurantId);
        ownerCheck.setRoleType("RestaurantOwner");

        AssignedRoleId adminCheck = new AssignedRoleId();
        adminCheck.setUserId(requesterId);
        adminCheck.setRestaurantId(restaurantId);
        adminCheck.setRoleType("RestaurantAdmin");

        return assignedRoleRepository.existsById(ownerCheck) || assignedRoleRepository.existsById(adminCheck);
    }

    public FoodVariantResponseDto getVariantById(Integer variantId) {
        Optional<FoodVariant> variantOpt = foodVariantRepository.findById(variantId);

        if (variantOpt.isEmpty()) {
            return null;
        }

        FoodVariant v = variantOpt.get();
        FoodVariantResponseDto dto = new FoodVariantResponseDto();
        dto.setId(v.getId());
        dto.setVariantName(v.getVariantName());
        dto.setPrice(v.getPrice());
        dto.setImageUrl(v.getImageUrl());
        dto.setStockQuantity(v.getStockQuantity());
        dto.setAvailabilityStatus(v.getAvailabilityStatus());

        return dto;
    }
}