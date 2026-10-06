package com.example.food_ordering_system.service;

import com.example.food_ordering_system.dto.RestaurantReviewDto;
import com.example.food_ordering_system.entity.RestaurantReview;
import com.example.food_ordering_system.repository.RestaurantReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantReviewService {

    private final RestaurantReviewRepository restaurantReviewRepository;

    public RestaurantReviewService(RestaurantReviewRepository restaurantReviewRepository) {
        this.restaurantReviewRepository = restaurantReviewRepository;
    }

    public RestaurantReview addReview(RestaurantReviewDto dto) {
        RestaurantReview review = new RestaurantReview(
                dto.getUserId(),
                dto.getRestaurantId(),
                dto.getRating(),
                dto.getComment()
        );
        return restaurantReviewRepository.save(review);
    }

    public List<RestaurantReview> getReviewsByRestaurant(Long restaurantId) {
        return restaurantReviewRepository.findByRestaurantId(restaurantId);
    }
}
