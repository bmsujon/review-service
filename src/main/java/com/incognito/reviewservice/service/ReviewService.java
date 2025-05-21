package com.incognito.reviewservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.incognito.reviewservice.exception.BadRequestException;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.incognito.reviewservice.dto.ReviewCreateRequest;
import com.incognito.reviewservice.dto.ReviewResponse;
import com.incognito.reviewservice.dto.ReviewStatsResponse;
import com.incognito.reviewservice.entity.Review;
import com.incognito.reviewservice.exception.ResourceNotFoundException;
import com.incognito.reviewservice.model.ReviewStatus;
import com.incognito.reviewservice.model.ReviewType;
import com.incognito.reviewservice.repository.ReviewRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request) {
        if (request.workEndDate() != null) {
            if (request.workStartDate() == null) {
                // If workEndDate is provided, workStartDate must also be provided for a valid comparison.
                throw new BadRequestException("Work start date must be provided if work end date is specified.");
            }
            if (!request.workEndDate().isAfter(request.workStartDate())) {
                throw new BadRequestException("Work end date must be after work start date.");
            }
        }

        Review review = Review.builder()
                .reviewType(request.reviewType())
                .title(request.title())
                .contentHtml(request.content())
                .ipAddress(request.ipAddress())
                .isEmployee(request.isEmployee())
                .dept(request.dept())
                .role(request.role())
                .companyName(request.companyName())
                .website(request.website())
                .workStartDate(request.workStartDate())
                .workEndDate(request.workEndDate())
                .status(ReviewStatus.PENDING)
                .reviewerName(ObjectUtils.isEmpty(request.reviewerName()) ? "Anonymous" : request.reviewerName())
                .build();
        Review savedReview = reviewRepository.save(review);
        return mapToReviewResponse(savedReview);
    }

    @Transactional
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
        return new ReviewResponse(
                review.getId(),
                review.getReviewType(),
                review.getTitle(),
                review.getContentHtml(),
                review.getIpAddress(),
                review.getLikeCount(),
                review.getDislikeCount(),
                review.hasAnyComment(), // Corrected to hasAnyComment()
                review.getStatus(),
                review.getIsEmployee(),
                review.getDept(),
                review.getRole(),
                review.getCompanyName(),
                review.getWebsite(),
                review.getWorkStartDate(),
                review.getWorkEndDate(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                review.getReviewerName(),
                review.getTotalComments() == null ? 0 : review.getTotalComments() // Handle null totalComments
        );
    }

    @Transactional(readOnly = true)
    public ReviewStatsResponse getReviewStats(String companyName, ReviewType reviewType) {
        // Use the new repository methods for optimized statistics calculation
        long totalReviews = reviewRepository.countFilteredReviews(companyName, reviewType);

        if (totalReviews == 0) {
            return ReviewStatsResponse.builder()
                    .totalReviews(0)
                    .countByReviewType(Map.of())
                    .totalLikes(0)
                    .totalDislikes(0)
                    .averageLikesPerReview(0.0)
                    .averageDislikesPerReview(0.0)
                    .build();
        }

        Long totalLikesLong = reviewRepository.sumLikesFiltered(companyName, reviewType);
        long totalLikes = (totalLikesLong == null) ? 0L : totalLikesLong;

        Long totalDislikesLong = reviewRepository.sumDislikesFiltered(companyName, reviewType);
        long totalDislikes = (totalDislikesLong == null) ? 0L : totalDislikesLong;

        List<Object[]> reviewCountsByTypeRaw = reviewRepository.getReviewCountsByTypeFiltered(companyName, reviewType);
        Map<ReviewType, Long> countByReviewType = reviewCountsByTypeRaw.stream()
                .collect(Collectors.toMap(
                        arr -> ReviewType.valueOf((String) arr[0]), // Convert String to ReviewType enum
                        arr -> (Long) arr[1]
                ));

        Double averageLikesPerReview = totalReviews > 0 ? (double) totalLikes / totalReviews : 0.0;
        Double averageDislikesPerReview = totalReviews > 0 ? (double) totalDislikes / totalReviews : 0.0;

        return ReviewStatsResponse.builder()
                .totalReviews(totalReviews)
                .countByReviewType(countByReviewType)
                .totalLikes(totalLikes)
                .totalDislikes(totalDislikes)
                .averageLikesPerReview(averageLikesPerReview)
                .averageDislikesPerReview(averageDislikesPerReview)
                .build();
    }
}