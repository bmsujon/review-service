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
                .contentHtml("Loved working here")
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

        reviewCreateRequest = new ReviewCreateRequest(
                ReviewType.NEGATIVE,
                "Great Company",
                "Loved working here",
                "127.0.0.1",
                "Engineering",
                "Software Engineer",
                "Incognito Corp",
                "http://incognito.com",
                true,
                fixedWorkStartDate,
                fixedWorkEndDate
        );
    }

    @Test
    void createReview_shouldSaveAndReturnReview() {
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse reviewResponse = reviewService.createReview(reviewCreateRequest);

        assertNotNull(reviewResponse);
        assertEquals(review.getReviewType(), reviewResponse.reviewType());
        assertEquals(review.getTitle(), reviewResponse.title());
        assertEquals(review.getContentHtml(), reviewResponse.contentHtml()); // Assuming ReviewResponse maps contentHtml to content
        assertEquals(review.getIpAddress(), reviewResponse.ipAddress());
        assertEquals(review.getIsEmployee(), reviewResponse.isEmployee());
        assertEquals(review.getDept(), reviewResponse.dept());
        assertEquals(review.getRole(), reviewResponse.role());
        assertEquals(review.getCompanyName(), reviewResponse.companyName());
        assertEquals(review.getWebsite(), reviewResponse.website());
        assertEquals(review.getWorkStartDate(), reviewResponse.workStartDate());
        assertEquals(review.getWorkEndDate(), reviewResponse.workEndDate());
        assertEquals(review.getLikeCount(), reviewResponse.likeCount());
        assertEquals(review.getDislikeCount(), reviewResponse.dislikeCount());
        assertEquals(review.getHasComment(), reviewResponse.hasComment());
        assertEquals(review.getStatus(), reviewResponse.status());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void getReviewById_whenReviewExists_shouldReturnReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        ReviewResponse reviewResponse = reviewService.getReviewById(1L);

        assertNotNull(reviewResponse);
        assertEquals(review.getId(), reviewResponse.id());
        assertEquals(review.getReviewType(), reviewResponse.reviewType());
        assertEquals(review.getTitle(), reviewResponse.title());
        assertEquals(review.getContentHtml(), reviewResponse.contentHtml());
        assertEquals(review.getIpAddress(), reviewResponse.ipAddress());
        assertEquals(review.getIsEmployee(), reviewResponse.isEmployee());
        assertEquals(review.getDept(), reviewResponse.dept());
        assertEquals(review.getRole(), reviewResponse.role());
        assertEquals(review.getCompanyName(), reviewResponse.companyName());
        assertEquals(review.getWebsite(), reviewResponse.website());
        assertEquals(review.getWorkStartDate(), reviewResponse.workStartDate());
        assertEquals(review.getWorkEndDate(), reviewResponse.workEndDate());
        assertEquals(review.getLikeCount(), reviewResponse.likeCount());
        assertEquals(review.getDislikeCount(), reviewResponse.dislikeCount());
        assertEquals(review.getHasComment(), reviewResponse.hasComment());
        assertEquals(review.getStatus(), reviewResponse.status());
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
        assertFalse(reviewResponsePage.getContent().isEmpty());
        ReviewResponse firstReview = reviewResponsePage.getContent().get(0);
        assertEquals(review.getCompanyName(), firstReview.companyName());
        assertEquals(review.getReviewType(), firstReview.reviewType());
        assertEquals(review.getTitle(), firstReview.title());
        assertEquals(review.getStatus(), firstReview.status());
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
        assertFalse(reviewResponsePage.getContent().isEmpty());
        ReviewResponse firstReview = reviewResponsePage.getContent().get(0);
        assertEquals(review.getCompanyName(), firstReview.companyName());
        assertEquals(review.getTitle(), firstReview.title());
        // Add more assertions as needed for the "no filter" case
        verify(reviewRepository, times(1)).findAll(any(Specification.class), eq(pageable)); // Suppressed warning applies here
    }

    @Test
    void incrementLikeCount_whenReviewExists_shouldIncrementAndReturnReview() {
        // Arrange

        Review reviewAfterLike = Review.builder() // Simulate the state after like increment
                .id(1L)
                .reviewType(ReviewType.POSITIVE)
                .title("Great Company")
                .contentHtml("Loved working here")
                .ipAddress("127.0.0.1")
                .isEmployee(true)
                .dept("Engineering")
                .role("Software Engineer")
                .companyName("Incognito Corp")
                .website("http://incognito.com")
                .workStartDate(fixedWorkStartDate)
                .workEndDate(fixedWorkEndDate)
                .likeCount(11) // Expected like count after increment
                .dislikeCount(1)
                .hasComment(false)
                .status(ReviewStatus.APPROVED)
                .build();

        when(reviewRepository.incrementLikeCount(1L)).thenReturn(1); // Simulate successful DB update
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(reviewAfterLike)); // Return the review with updated count

        // Act
        ReviewResponse reviewResponse = reviewService.incrementLikeCount(1L);

        // Assert
        assertNotNull(reviewResponse);
        assertEquals(reviewAfterLike.getId(), reviewResponse.id());
        assertEquals(reviewAfterLike.getLikeCount(), reviewResponse.likeCount()); // Assert updated like count
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
        // Arrange

        Review reviewAfterDislike = Review.builder() // Simulate the state after dislike increment
                .id(1L)
                .reviewType(ReviewType.POSITIVE)
                .title("Great Company")
                .contentHtml("Loved working here")
                .ipAddress("127.0.0.1")
                .isEmployee(true)
                .dept("Engineering")
                .role("Software Engineer")
                .companyName("Incognito Corp")
                .website("http://incognito.com")
                .workStartDate(fixedWorkStartDate)
                .workEndDate(fixedWorkEndDate)
                .likeCount(10)
                .dislikeCount(2) // Expected dislike count after increment
                .hasComment(false)
                .status(ReviewStatus.APPROVED)
                .build();

        when(reviewRepository.incrementDislikeCount(1L)).thenReturn(1); // Simulate successful DB update
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(reviewAfterDislike)); // Return the review with updated count

        // Act
        ReviewResponse reviewResponse = reviewService.incrementDislikeCount(1L);

        // Assert
        assertNotNull(reviewResponse);
        assertEquals(reviewAfterDislike.getId(), reviewResponse.id());
        assertEquals(reviewAfterDislike.getDislikeCount(), reviewResponse.dislikeCount()); // Assert updated dislike count
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
