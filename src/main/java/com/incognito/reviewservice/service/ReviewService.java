package com.incognito.reviewservice.service;

import com.incognito.reviewservice.dto.ReviewCreateRequest;
import com.incognito.reviewservice.dto.ReviewResponse;
import com.incognito.reviewservice.entity.Review;
import com.incognito.reviewservice.exception.ResourceNotFoundException;
import com.incognito.reviewservice.model.ReviewType;
import com.incognito.reviewservice.repository.ReviewRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request) {
        Review.ReviewBuilder reviewBuilder = Review.builder()
                .reviewType(request.getReviewType())
                .title(request.getTitle())
                .content(request.getContent())
                .ipAddress(request.getIpAddress())
                .dept(request.getDept())
                .role(request.getRole())
                .companyName(request.getCompanyName())
                .website(request.getWebsite())
                .workStartDate(request.getWorkStartDate())
                .workEndDate(request.getWorkEndDate());

        if (request.getIsEmployee() != null) {
            reviewBuilder.isEmployee(request.getIsEmployee());
        }

        Review review = reviewBuilder.build();
        Review savedReview = reviewRepository.save(review);
        return mapToReviewResponse(savedReview);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        return mapToReviewResponse(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviews(String companyName, ReviewType reviewType, Pageable pageable) {
        Specification<Review> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(companyName)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("companyName")), "%" + companyName.toLowerCase() + "%"));
            }
            if (reviewType != null) {
                predicates.add(criteriaBuilder.equal(root.get("reviewType"), reviewType));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Page<Review> reviewPage = reviewRepository.findAll(spec, pageable);
        return reviewPage.map(this::mapToReviewResponse);
    }

    /**
     * Increments the like count for a given review.
     *
     * @param reviewId The ID of the review to like.
     * @return A {@link ReviewResponse} representing the updated review.
     * @throws ResourceNotFoundException if no review is found with the given ID.
     */
    @Transactional
    public ReviewResponse incrementLikeCount(Long reviewId) {
        int updatedRows = reviewRepository.incrementLikeCount(reviewId);
        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Review not found with id: " + reviewId + " to increment like count.");
        }
        // Fetch the updated review to return the latest state
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId)); // Should not happen if update was successful
        return mapToReviewResponse(review);
    }

    /**
     * Increments the dislike count for a given review.
     *
     * @param reviewId The ID of the review to dislike.
     * @return A {@link ReviewResponse} representing the updated review.
     * @throws ResourceNotFoundException if no review is found with the given ID.
     */
    @Transactional
    public ReviewResponse incrementDislikeCount(Long reviewId) {
        int updatedRows = reviewRepository.incrementDislikeCount(reviewId);
        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Review not found with id: " + reviewId + " to increment dislike count.");
        }
        // Fetch the updated review to return the latest state
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId)); // Should not happen if update was successful
        return mapToReviewResponse(review);
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        if (review == null) {
            return null;
        }
        return ReviewResponse.builder()
                .id(review.getId())
                .reviewType(review.getReviewType())
                .title(review.getTitle())
                .content(review.getContent())
                .ipAddress(review.getIpAddress())
                .likeCount(review.getLikeCount())
                .dislikeCount(review.getDislikeCount())
                .hasComment(review.getHasComment())
                .status(review.getStatus())
                .isEmployee(review.getIsEmployee())
                .dept(review.getDept())
                .role(review.getRole())
                .companyName(review.getCompanyName())
                .website(review.getWebsite())
                .workStartDate(review.getWorkStartDate())
                .workEndDate(review.getWorkEndDate())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}