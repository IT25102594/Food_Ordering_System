package com.example.food_ordering_system.dto;

public class RestaurantReviewDto {
    private Long userId;
    private Long restaurantId;
    private Integer rating;
    private String comment;

    public RestaurantReviewDto() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
//comment
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}

