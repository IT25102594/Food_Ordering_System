package com.example.food_ordering_system.repository;

import com.example.food_ordering_system.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    Optional<CartItem> findByCart_IdAndFoodVariant_Id(Integer cartId, Integer foodVariantId);
}