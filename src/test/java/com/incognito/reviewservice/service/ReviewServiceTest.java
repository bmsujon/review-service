package com.incognito.reviewservice.service;

import com.incognito.reviewservice.dto.ReviewCreateRequest;
import com.incognito.reviewservice.dto.ReviewResponse;
import com.incognito.reviewservice.entity.Review;
import com.incognito.reviewservice.exception.ResourceNotFoundException;
import com.incognito.reviewservice.model.ReviewStatus;
import com.incognito.reviewservice.model.ReviewType;
import com.incognito.reviewservice.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant; // Changed from LocalDate
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Review review;
    private ReviewCreateRequest reviewCreateRequest;

    private final Instant fixedWorkStartDate = LocalDate.of(2023, 1, 15).atStartOfDay().toInstant(java.time.ZoneOffset.UTC); // Corrected type and initialization
    private final Instant fixedWorkEndDate = LocalDate.of(2024, 1, 15).atStartOfDay().toInstant(java.time.ZoneOffset.UTC); // Corrected type and initialization

    @BeforeEach
    void setUp() {
        review = Review.builder()
                .id(1L)
                .reviewType(ReviewType.POSITIVE)
                .title("Great Company")
                .content("Loved working here")
                .ipAddress("127.0.0.1")
                .isEmployee(true)
                .dept("Engineering")
                .role("Software Engineer")
                .companyName("Incognito Corp")
                .website("http://incognito.com")
                .workStartDate(fixedWorkStartDate)
                .workEndDate(fixedWorkEndDate)
                .likeCount(10)
                .dislikeCount(1)
                .hasComment(false)
                .status(ReviewStatus.APPROVED)
                .build();

        reviewCreateRequest = ReviewCreateRequest.builder()
                .reviewType(ReviewType.NEGATIVE)
                .title("Great Company")
                .content("Loved working here")
                .ipAddress("127.0.0.1")
                .isEmployee(true)
                .dept("Engineering")
                .role("Software Engineer")
                .companyName("Incognito Corp")
                .website("http://incognito.com")
                .workStartDate(fixedWorkStartDate)
                .workEndDate(fixedWorkEndDate)
                .build();
    }

    @Test
    void createReview_shouldSaveAndReturnReview() {
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse reviewResponse = reviewService.createReview(reviewCreateRequest);

        assertNotNull(reviewResponse);
        assertEquals(review.getTitle(), reviewResponse.getTitle());
        assertEquals(review.getCompanyName(), reviewResponse.getCompanyName());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void getReviewById_whenReviewExists_shouldReturnReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        ReviewResponse reviewResponse = reviewService.getReviewById(1L);

        assertNotNull(reviewResponse);
        assertEquals(review.getId(), reviewResponse.getId());
        verify(reviewRepository, times(1)).findById(1L);
    }

    @Test
    void getReviewById_whenReviewNotFound_shouldThrowResourceNotFoundException() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewById(1L));
        verify(reviewRepository, times(1)).findById(1L);
    }

    @Test
    @SuppressWarnings("unchecked") // Added to suppress Specification warning
    void getReviews_shouldReturnPageOfReviews() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(Collections.singletonList(review), pageable, 1);
        when(reviewRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(reviewPage);

        Page<ReviewResponse> reviewResponsePage = reviewService.getReviews("Incognito Corp", ReviewType.POSITIVE, pageable);

        assertNotNull(reviewResponsePage);
        assertEquals(1, reviewResponsePage.getTotalElements());
        assertEquals(review.getCompanyName(), reviewResponsePage.getContent().get(0).getCompanyName());
        verify(reviewRepository, times(1)).findAll(any(Specification.class), any(Pageable.class)); // Suppressed warning applies here
    }
    
    @Test
    @SuppressWarnings("unchecked") // Added to suppress Specification warning
    void getReviews_whenNoFilters_shouldReturnPageOfReviews() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(Collections.singletonList(review), pageable, 1);
        // Ensure the mock for findAll without specific filters is set up
        when(reviewRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(reviewPage);

        Page<ReviewResponse> reviewResponsePage = reviewService.getReviews(null, null, pageable);

        assertNotNull(reviewResponsePage);
        assertEquals(1, reviewResponsePage.getTotalElements());
        assertEquals(review.getCompanyName(), reviewResponsePage.getContent().get(0).getCompanyName());
        verify(reviewRepository, times(1)).findAll(any(Specification.class), eq(pageable)); // Suppressed warning applies here
    }


    @Test
    void incrementLikeCount_whenReviewExists_shouldIncrementAndReturnReview() {
        when(reviewRepository.incrementLikeCount(1L)).thenReturn(1);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        ReviewResponse reviewResponse = reviewService.incrementLikeCount(1L);

        assertNotNull(reviewResponse);
        assertEquals(review.getId(), reviewResponse.getId());
        verify(reviewRepository, times(1)).incrementLikeCount(1L);
        verify(reviewRepository, times(1)).findById(1L);
    }

    @Test
    void incrementLikeCount_whenReviewNotFoundForIncrement_shouldThrowResourceNotFoundException() {
        when(reviewRepository.incrementLikeCount(1L)).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> reviewService.incrementLikeCount(1L));
        verify(reviewRepository, times(1)).incrementLikeCount(1L);
        verify(reviewRepository, never()).findById(1L);
    }
    
    @Test
    void incrementLikeCount_whenReviewNotFoundForFindById_shouldThrowResourceNotFoundException() {
        when(reviewRepository.incrementLikeCount(1L)).thenReturn(1); // Assume update was successful
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty()); // But then review not found

        assertThrows(ResourceNotFoundException.class, () -> reviewService.incrementLikeCount(1L));
        verify(reviewRepository, times(1)).incrementLikeCount(1L);
        verify(reviewRepository, times(1)).findById(1L);
    }

    @Test
    void incrementDislikeCount_whenReviewExists_shouldIncrementAndReturnReview() {
        when(reviewRepository.incrementDislikeCount(1L)).thenReturn(1);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        ReviewResponse reviewResponse = reviewService.incrementDislikeCount(1L);

        assertNotNull(reviewResponse);
        assertEquals(review.getId(), reviewResponse.getId());
        verify(reviewRepository, times(1)).incrementDislikeCount(1L);
        verify(reviewRepository, times(1)).findById(1L);
    }

    @Test
    void incrementDislikeCount_whenReviewNotFoundForIncrement_shouldThrowResourceNotFoundException() {
        when(reviewRepository.incrementDislikeCount(1L)).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> reviewService.incrementDislikeCount(1L));
        verify(reviewRepository, times(1)).incrementDislikeCount(1L);
        verify(reviewRepository, never()).findById(1L);
    }
    
    @Test
    void incrementDislikeCount_whenReviewNotFoundForFindById_shouldThrowResourceNotFoundException() {
        when(reviewRepository.incrementDislikeCount(1L)).thenReturn(1);
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.incrementDislikeCount(1L));
        verify(reviewRepository, times(1)).incrementDislikeCount(1L);
        verify(reviewRepository, times(1)).findById(1L);
    }
}
