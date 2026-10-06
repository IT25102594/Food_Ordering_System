package com.example.food_ordering_system.service;

import com.example.food_ordering_system.dto.UserPaymentMethodRequestDto;
import com.example.food_ordering_system.dto.UserPaymentMethodResponseDto;
import com.example.food_ordering_system.entity.User;
import com.example.food_ordering_system.entity.UserPaymentMethod;
import com.example.food_ordering_system.repository.UserPaymentMethodRepository;
import com.example.food_ordering_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPaymentMethodService {

    private final UserPaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserPaymentMethodResponseDto addPaymentMethod(Integer userId, UserPaymentMethodRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (Boolean.TRUE.equals(requestDto.getIsDefault())) {
            paymentMethodRepository.resetDefaultPaymentMethods(userId);
        }

        UserPaymentMethod paymentMethod = new UserPaymentMethod();
        paymentMethod.setUser(user);
        paymentMethod.setLabel(requestDto.getLabel());
        paymentMethod.setGatewayProvider("Stripe");
        paymentMethod.setPaymentToken(requestDto.getPaymentToken());
        paymentMethod.setCardBrand(requestDto.getCardBrand());
        paymentMethod.setLastFourDigits(requestDto.getLastFourDigits());
        paymentMethod.setExpMonth(requestDto.getExpMonth());
        paymentMethod.setExpYear(requestDto.getExpYear());
        paymentMethod.setIsDefault(requestDto.getIsDefault() != null ? requestDto.getIsDefault() : false);
        paymentMethod.setCreatedAt(Instant.now());

        UserPaymentMethod savedMethod = paymentMethodRepository.save(paymentMethod);
        return mapToResponseDto(savedMethod);
    }

    @Transactional(readOnly = true)
    public List<UserPaymentMethodResponseDto> getUserPaymentMethods(Integer userId) {
        return paymentMethodRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePaymentMethod(Integer userId, Integer methodId) {
        UserPaymentMethod paymentMethod = paymentMethodRepository.findById(methodId)
                .orElseThrow(() -> new RuntimeException("Payment method not found"));

        if (!paymentMethod.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this payment method");
        }

        paymentMethodRepository.delete(paymentMethod);
    }

    @Transactional
    public UserPaymentMethodResponseDto setAsDefault(Integer userId, Integer methodId) {
        UserPaymentMethod paymentMethod = paymentMethodRepository.findById(methodId)
                .orElseThrow(() -> new RuntimeException("Payment method not found"));

        if (!paymentMethod.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to modify this payment method");
        }

        paymentMethodRepository.resetDefaultPaymentMethods(userId);
        paymentMethod.setIsDefault(true);

        UserPaymentMethod savedMethod = paymentMethodRepository.save(paymentMethod);
        return mapToResponseDto(savedMethod);
    }

    private UserPaymentMethodResponseDto mapToResponseDto(UserPaymentMethod entity) {
        UserPaymentMethodResponseDto dto = new UserPaymentMethodResponseDto();
        dto.setId(entity.getId());
        dto.setLabel(entity.getLabel());
        dto.setGatewayProvider(entity.getGatewayProvider());
        dto.setCardBrand(entity.getCardBrand());
        dto.setLastFourDigits(entity.getLastFourDigits());
        dto.setExpMonth(entity.getExpMonth());
        dto.setExpYear(entity.getExpYear());
        dto.setIsDefault(entity.getIsDefault());
        // deleted the dto.setCreatedAt line
        return dto;
    }
}