package com.example.food_ordering_system.service;

import com.example.food_ordering_system.dto.FoodItemRequestDto;
import com.example.food_ordering_system.dto.FoodItemResponseDto;
import com.example.food_ordering_system.dto.FoodVariantRequestDto;
import com.example.food_ordering_system.dto.FoodVariantResponseDto;
import com.example.food_ordering_system.entity.*;
import com.example.food_ordering_system.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FoodItemService {

    private final FoodItemRepository foodItemRepository;
    private final FoodVariantRepository foodVariantRepository;
    private final FoodCategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final AssignedRoleRepository assignedRoleRepository;

    public FoodItemService(FoodItemRepository foodItemRepository,
                           FoodVariantRepository foodVariantRepository,
                           FoodCategoryRepository categoryRepository,
                           RestaurantRepository restaurantRepository,
                           AssignedRoleRepository assignedRoleRepository) {
        this.foodItemRepository = foodItemRepository;
        this.foodVariantRepository = foodVariantRepository;
        this.categoryRepository = categoryRepository;
        this.restaurantRepository = restaurantRepository;
        this.assignedRoleRepository = assignedRoleRepository;
    }

    @Transactional
    public String createFoodItem(Integer restaurantId, Integer requesterId, FoodItemRequestDto dto) {
        if (!isAuthorizedToEdit(requesterId, restaurantId)) {
            return "Error: Unauthorized. Only Restaurant Owners or Admins can modify the menu.";
        }

        Optional<Restaurant> restOpt = restaurantRepository.findById(restaurantId);
        if (restOpt.isEmpty()) return "Error: Restaurant not found.";

        FoodItem foodItem = new FoodItem();
        foodItem.setName(dto.getName());
        foodItem.setDescription(dto.getDescription());
        foodItem.setRestaurant(restOpt.get());

        if (dto.getCategoryId() != null) {
            Optional<FoodCategory> catOpt = categoryRepository.findById(dto.getCategoryId());
            if (catOpt.isEmpty()) return "Error: Food category not found.";
            foodItem.setCategory(catOpt.get());
        } else {
            foodItem.setCategory(null);
        }
        foodItem.setAvgRating(java.math.BigDecimal.ZERO);

        foodItem = foodItemRepository.save(foodItem);

        if (dto.getVariants() != null && !dto.getVariants().isEmpty()) {
            for (FoodVariantRequestDto variantDto : dto.getVariants()) {
                FoodVariant variant = new FoodVariant();
                variant.setFoodItem(foodItem);
                variant.setVariantName(variantDto.getVariantName());
                variant.setPrice(variantDto.getPrice());
                variant.setImageUrl(variantDto.getImageUrl()); // Save variant image
                variant.setStockQuantity(variantDto.getStockQuantity() != null ? variantDto.getStockQuantity() : -1);
                variant.setAvailabilityStatus(variantDto.getAvailabilityStatus() != null ? variantDto.getAvailabilityStatus() : true);
                foodVariantRepository.save(variant);
            }
        } else {
            return "Error: A food item must have at least one variant (e.g., 'Regular' size).";
        }

        return "Success: Food item '" + foodItem.getName() + "' created with variants!";
    }

    public List<FoodItemResponseDto> getMenuByRestaurant(Integer restaurantId) {
        return foodItemRepository.findByRestaurant_Id(restaurantId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public FoodItemResponseDto getFoodItemById(Integer itemId) {
        Optional<FoodItem> itemOpt = foodItemRepository.findById(itemId);
        return itemOpt.map(this::mapToResponseDto).orElse(null);
    }

    public String updateFoodItem(Integer itemId, Integer requesterId, FoodItemRequestDto dto) {
        Optional<FoodItem> itemOpt = foodItemRepository.findById(itemId);
        if (itemOpt.isEmpty()) return "Error: Food item not found.";

        FoodItem foodItem = itemOpt.get();

        if (!isAuthorizedToEdit(requesterId, foodItem.getRestaurant().getId())) {
            return "Error: Unauthorized. Only Restaurant Owners or Admins can modify the menu.";
        }

        if (dto.getName() != null) foodItem.setName(dto.getName());
        if (dto.getDescription() != null) foodItem.setDescription(dto.getDescription());

        if (dto.getCategoryId() != null) {
            Optional<FoodCategory> catOpt = categoryRepository.findById(dto.getCategoryId());
            if (catOpt.isEmpty()) return "Error: Food category not found.";
            foodItem.setCategory(catOpt.get());
        }

        foodItemRepository.save(foodItem);
        return "Success: Food item details updated.";
    }

    public String deleteFoodItem(Integer itemId, Integer requesterId) {
        Optional<FoodItem> itemOpt = foodItemRepository.findById(itemId);
        if (itemOpt.isEmpty()) return "Error: Food item not found.";

        FoodItem foodItem = itemOpt.get();

        if (!isAuthorizedToEdit(requesterId, foodItem.getRestaurant().getId())) {
            return "Error: Unauthorized. Only Restaurant Owners or Admins can modify the menu.";
        }

        foodItemRepository.delete(foodItem);
        return "Success: Food item and its associated variants deleted.";
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

    private FoodItemResponseDto mapToResponseDto(FoodItem item) {
        FoodItemResponseDto dto = new FoodItemResponseDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvgRating(item.getAvgRating());

        if (item.getCategory() != null) {
            dto.setCategoryId(item.getCategory().getId());
            dto.setCategoryName(item.getCategory().getName());
        }

        List<FoodVariant> variants = foodVariantRepository.findByFoodItem_Id(item.getId());

        // Find the cheapest variant to use as the default display
        variants.stream()
                .min(Comparator.comparing(FoodVariant::getPrice))
                .ifPresent(cheapestVariant -> {
                    dto.setDefaultPrice(cheapestVariant.getPrice());
                    dto.setDefaultImageUrl(cheapestVariant.getImageUrl());
                });

        List<FoodVariantResponseDto> variantDtos = variants.stream().map(v -> {
            FoodVariantResponseDto vDto = new FoodVariantResponseDto();
            vDto.setId(v.getId());
            vDto.setVariantName(v.getVariantName());
            vDto.setPrice(v.getPrice());
            vDto.setImageUrl(v.getImageUrl()); // Map the variant image
            vDto.setStockQuantity(v.getStockQuantity());
            vDto.setAvailabilityStatus(v.getAvailabilityStatus());
            return vDto;
        }).collect(Collectors.toList());

        dto.setVariants(variantDtos);
        return dto;
    }
}