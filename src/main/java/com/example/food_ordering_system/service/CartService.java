package com.example.food_ordering_system.service;

import com.example.food_ordering_system.dto.AddToCartRequestDto;
import com.example.food_ordering_system.dto.CartResponseDto;
import com.example.food_ordering_system.dto.CartItemDto;
import com.example.food_ordering_system.entity.Cart;
import com.example.food_ordering_system.entity.CartItem;
import com.example.food_ordering_system.entity.FoodVariant;
import com.example.food_ordering_system.entity.Restaurant;
import com.example.food_ordering_system.repository.CartItemRepository;
import com.example.food_ordering_system.repository.CartRepository;
import com.example.food_ordering_system.repository.FoodVariantRepository;
import com.example.food_ordering_system.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final FoodVariantRepository foodVariantRepository;
    private final RestaurantRepository restaurantRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       FoodVariantRepository foodVariantRepository, RestaurantRepository restaurantRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.foodVariantRepository = foodVariantRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public String addToCart(Integer userId, AddToCartRequestDto dto) {
        if (dto.getVariantId() == null) return "Error: Variant ID is missing from request.";
        if (dto.getRestaurantId() == null) return "Error: Restaurant ID is missing from request.";

        FoodVariant variant = foodVariantRepository.findById(dto.getVariantId())
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        if (!variant.getAvailabilityStatus()) {
            return "Error: This item is currently unavailable.";
        }

        // Fetch or create the user's cart
        Optional<Cart> existingCartOpt = cartRepository.findByUserId(userId);
        Cart cart;

        if (existingCartOpt.isPresent()) {
            cart = existingCartOpt.get();
            // Restrict cart to a single restaurant
            if (!cart.getRestaurant().getId().equals(dto.getRestaurantId())) {
                return "DIFFERENT_RESTAURANT"; // A specific flag the frontend can catch to prompt clearing the cart
            }
        } else {
            Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));
            cart = new Cart();
            cart.setUserId(userId);
            cart.setRestaurant(restaurant);
            cart = cartRepository.save(cart);
        }

        // Check if item is already in cart to update quantity, otherwise create new
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCart_IdAndFoodVariant_Id(cart.getId(), variant.getId());

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + dto.getQuantity();

            if (variant.getStockQuantity() != -1 && newQuantity > variant.getStockQuantity()) {
                return "Error: Cannot exceed available stock of " + variant.getStockQuantity();
            }

            existingItem.setQuantity(newQuantity);
            if (dto.getSpecialInstructions() != null) {
                existingItem.setSpecialInstructions(dto.getSpecialInstructions());
            }
            cartItemRepository.save(existingItem);
            return "Success: Cart updated.";
        } else {
            if (variant.getStockQuantity() != -1 && dto.getQuantity() > variant.getStockQuantity()) {
                return "Error: Cannot exceed available stock.";
            }

            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setFoodVariant(variant);
            newItem.setQuantity(dto.getQuantity());
            newItem.setSpecialInstructions(dto.getSpecialInstructions());
            cartItemRepository.save(newItem);
            return "Success: Item added to cart.";
        }
    }

    @Transactional
    public void clearCart(Integer userId) {
        cartRepository.findByUserId(userId).ifPresent(cartRepository::delete);
    }


    @Transactional(readOnly = true)
    public CartResponseDto getCart(Integer userId) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Cart empty"));
        CartResponseDto dto = new CartResponseDto();
        dto.setId(cart.getId());
        dto.setRestaurantId(cart.getRestaurant().getId());
        dto.setRestaurantName(cart.getRestaurant().getName());

        dto.setItems(cart.getItems().stream().map(item -> {
            // REMOVED the "CartResponseDto." prefix here:
            CartItemDto itemDto = new CartItemDto();

            itemDto.setCartItemId(item.getId());
            itemDto.setFoodVariantId(item.getFoodVariant().getId());
            itemDto.setItemName(item.getFoodVariant().getFoodItem().getName());
            itemDto.setVariantName(item.getFoodVariant().getVariantName());
            itemDto.setPrice(item.getFoodVariant().getPrice());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setSpecialInstructions(item.getSpecialInstructions());
            itemDto.setImageUrl(item.getFoodVariant().getImageUrl());


            return itemDto;
        }).toList());

        return dto;
    }

    @Transactional
    public String removeCartItem(Integer userId, Integer variantId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user."));

        // Remove the specific item from the list
        boolean removed = cart.getItems().removeIf(item -> item.getFoodVariant().getId().equals(variantId));

        if (removed) {
            cartRepository.save(cart);
            return "Item successfully removed from cart.";
        } else {
            return "Item not found in your cart.";
        }
    }
}