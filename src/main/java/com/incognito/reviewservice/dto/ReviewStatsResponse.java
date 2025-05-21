package com.incognito.reviewservice.dto;

import java.util.Map;

import com.incognito.reviewservice.model.ReviewType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewStatsResponse {
    private long totalReviews;
    private Map<ReviewType, Long> countByReviewType;
    private long totalLikes;
    private long totalDislikes;
    private Double averageLikesPerReview;
    private Double averageDislikesPerReview;
}
