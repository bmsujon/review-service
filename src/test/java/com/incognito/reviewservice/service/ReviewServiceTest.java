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
    private ReviewCreateRequest reviewCreateRequestWithNullReviewer; // Added for new test

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
        review.setCreatedAt(Instant.now().minusSeconds(3600));
        review.setUpdatedAt(Instant.now().minusSeconds(1800));
        review.setTotalComments(0); // Explicitly set for tests expecting a non-null value from the base 'review'

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
                fixedWorkEndDate,
                "Test Reviewer From Request" // Added reviewerName
        );

        reviewCreateRequestWithNullReviewer = new ReviewCreateRequest( // Added for new test
                ReviewType.POSITIVE,
                "Okay Company",
                "It was alright",
                "192.168.1.1",
                "Sales",
                "Sales Rep",
                "Another Corp",
                "http://another.com",
                false,
                fixedWorkStartDate,
                fixedWorkEndDate,
                null // Null reviewerName
        );
    }

    @Test
    void createReview_shouldSaveAndReturnReview() {
        // Arrange
        Instant testInstant = Instant.now();
        Review expectedSavedReview = Review.builder()
                .id(1L) // Assuming an ID is assigned upon saving
                .reviewType(reviewCreateRequest.reviewType())
                .title(reviewCreateRequest.title())
                .contentHtml(reviewCreateRequest.content())
                .ipAddress(reviewCreateRequest.ipAddress())
                .isEmployee(reviewCreateRequest.isEmployee())
                .dept(reviewCreateRequest.dept())
                .role(reviewCreateRequest.role())
                .companyName(reviewCreateRequest.companyName())
                .website(reviewCreateRequest.website())
                .workStartDate(reviewCreateRequest.workStartDate())
                .workEndDate(reviewCreateRequest.workEndDate())
                .reviewerName(reviewCreateRequest.reviewerName()) // Set from request
                .likeCount(0) // New reviews should start with 0 likes
                .dislikeCount(0) // New reviews should start with 0 dislikes
                .hasComment(false) // New reviews typically don't have comments initially
                .status(ReviewStatus.PENDING) // Assuming PENDING is the initial status for new reviews
                .build();
        expectedSavedReview.setCreatedAt(testInstant);
        expectedSavedReview.setUpdatedAt(testInstant);

        when(reviewRepository.save(any(Review.class))).thenReturn(expectedSavedReview);

        // Act
        ReviewResponse reviewResponse = reviewService.createReview(reviewCreateRequest);

        // Assert
        assertNotNull(reviewResponse);
        assertEquals(expectedSavedReview.getId(), reviewResponse.id());
        assertEquals(expectedSavedReview.getReviewType(), reviewResponse.reviewType());
        assertEquals(expectedSavedReview.getTitle(), reviewResponse.title());
        assertEquals(expectedSavedReview.getContentHtml(), reviewResponse.contentHtml());
        assertEquals(expectedSavedReview.getIpAddress(), reviewResponse.ipAddress());
        assertEquals(expectedSavedReview.getIsEmployee(), reviewResponse.isEmployee());
        assertEquals(expectedSavedReview.getDept(), reviewResponse.dept());
        assertEquals(expectedSavedReview.getRole(), reviewResponse.role());
        assertEquals(expectedSavedReview.getCompanyName(), reviewResponse.companyName());
        assertEquals(expectedSavedReview.getWebsite(), reviewResponse.website());
        assertEquals(expectedSavedReview.getWorkStartDate(), reviewResponse.workStartDate());
        assertEquals(expectedSavedReview.getWorkEndDate(), reviewResponse.workEndDate());
        assertEquals(expectedSavedReview.getLikeCount(), reviewResponse.likeCount());
        assertEquals(expectedSavedReview.getDislikeCount(), reviewResponse.dislikeCount());
        assertEquals(expectedSavedReview.getHasComment(), reviewResponse.hasComment());
        assertEquals(expectedSavedReview.getStatus(), reviewResponse.status());
        assertEquals(reviewCreateRequest.reviewerName(), reviewResponse.reviewerName()); // Assert reviewerName from request
        assertEquals(0, reviewResponse.totalComments()); // New review should have 0 comments
        assertNotNull(reviewResponse.createdAt()); // Should be populated by service/JPA
        assertNotNull(reviewResponse.updatedAt()); // Should be populated by service/JPA
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void createReview_shouldDefaultToAnonymous_whenReviewerNameIsNull() {
        // Arrange
        Instant testInstant = Instant.now();
        Review expectedSavedReview = Review.builder()
                .id(2L) // Different ID for this test case
                .reviewType(reviewCreateRequestWithNullReviewer.reviewType())
                .title(reviewCreateRequestWithNullReviewer.title())
                .contentHtml(reviewCreateRequestWithNullReviewer.content())
                .ipAddress(reviewCreateRequestWithNullReviewer.ipAddress())
                .isEmployee(reviewCreateRequestWithNullReviewer.isEmployee())
                .dept(reviewCreateRequestWithNullReviewer.dept())
                .role(reviewCreateRequestWithNullReviewer.role())
                .companyName(reviewCreateRequestWithNullReviewer.companyName())
                .website(reviewCreateRequestWithNullReviewer.website())
                .workStartDate(reviewCreateRequestWithNullReviewer.workStartDate())
                .workEndDate(reviewCreateRequestWithNullReviewer.workEndDate())
                .reviewerName("Anonymous") // Expected default
                .likeCount(0)
                .dislikeCount(0)
                .hasComment(false)
                .status(ReviewStatus.PENDING) // Expected PENDING status
                .build();
        expectedSavedReview.setCreatedAt(testInstant);
        expectedSavedReview.setUpdatedAt(testInstant);

        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review reviewToSave = invocation.getArgument(0);
            // Simulate ID assignment and timestamping by JPA
            if (reviewToSave.getId() == null) { // Check if it's the one from this test
                 reviewToSave.setId(expectedSavedReview.getId()); // Assign ID
            }
            reviewToSave.setCreatedAt(expectedSavedReview.getCreatedAt());
            reviewToSave.setUpdatedAt(expectedSavedReview.getUpdatedAt());
            // Crucially, ensure the reviewerName is what the service should have set
            assertEquals("Anonymous", reviewToSave.getReviewerName());
            assertEquals(ReviewStatus.PENDING, reviewToSave.getStatus());
            return reviewToSave; // Return the (potentially modified for test) review
        });

        // Act
        ReviewResponse reviewResponse = reviewService.createReview(reviewCreateRequestWithNullReviewer);

        // Assert
        assertNotNull(reviewResponse);
        assertEquals(expectedSavedReview.getId(), reviewResponse.id());
        assertEquals("Anonymous", reviewResponse.reviewerName());
        assertEquals(ReviewStatus.PENDING, reviewResponse.status());
        assertEquals(0, reviewResponse.totalComments());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void createReview_shouldSetStatusToPending() {
        // Arrange
        Instant testInstant = Instant.now();
        ReviewCreateRequest pendingStatusRequest = new ReviewCreateRequest(
                ReviewType.MIXED, "Test Title", "Test Content", "127.0.0.2",
                null, null, "Test Corp", null, false, null, null, "Tester"
        );
        Review expectedSavedReview = Review.builder()
                .id(3L)
                .reviewType(pendingStatusRequest.reviewType())
                .title(pendingStatusRequest.title())
                .contentHtml(pendingStatusRequest.content())
                .ipAddress(pendingStatusRequest.ipAddress())
                .reviewerName(pendingStatusRequest.reviewerName())
                .status(ReviewStatus.PENDING) // Explicitly checking this
                .likeCount(0).dislikeCount(0).hasComment(false)
                .build();
        expectedSavedReview.setCreatedAt(testInstant);
        expectedSavedReview.setUpdatedAt(testInstant);

        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review reviewToSave = invocation.getArgument(0);
            if (reviewToSave.getId() == null) {
                reviewToSave.setId(expectedSavedReview.getId());
            }
            reviewToSave.setCreatedAt(expectedSavedReview.getCreatedAt());
            reviewToSave.setUpdatedAt(expectedSavedReview.getUpdatedAt());
            assertEquals(ReviewStatus.PENDING, reviewToSave.getStatus()); // Verify status before saving
            return reviewToSave;
        });

        // Act
        ReviewResponse reviewResponse = reviewService.createReview(pendingStatusRequest);

        // Assert
        assertNotNull(reviewResponse);
        assertEquals(expectedSavedReview.getId(), reviewResponse.id());
        assertEquals(ReviewStatus.PENDING, reviewResponse.status());
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
        assertEquals(review.getReviewerName(), reviewResponse.reviewerName());
        assertEquals(0, reviewResponse.totalComments()); // DTO mapping handles null from entity, or it's 0 from setUp
        assertEquals(review.getCreatedAt(), reviewResponse.createdAt());
        assertEquals(review.getUpdatedAt(), reviewResponse.updatedAt());
        verify(reviewRepository, times(1)).findById(1L);
    }

    @Test
    void getReviewById_whenReviewNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        Long nonExistentReviewId = 999L;
        when(reviewRepository.findById(nonExistentReviewId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.getReviewById(nonExistentReviewId);
        });
        assertEquals("Review not found with id: " + nonExistentReviewId, exception.getMessage());
        verify(reviewRepository, times(1)).findById(nonExistentReviewId);
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
        assertEquals(review.getReviewerName(), firstReview.reviewerName());
        assertEquals(0, firstReview.totalComments()); // DTO mapping handles null from entity, or it's 0 from setUp
        assertEquals(review.getCreatedAt(), firstReview.createdAt());
        assertEquals(review.getUpdatedAt(), firstReview.updatedAt());
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
        assertEquals(review.getReviewerName(), firstReview.reviewerName());
        assertEquals(0, firstReview.totalComments()); // DTO mapping handles null from entity, or it's 0 from setUp
        assertEquals(review.getCreatedAt(), firstReview.createdAt());
        assertEquals(review.getUpdatedAt(), firstReview.updatedAt());
        verify(reviewRepository, times(1)).findAll(any(Specification.class), eq(pageable)); // Suppressed warning applies here
    }

    @Test
    @SuppressWarnings("unchecked")
    void getReviews_withCompanyNameFilterOnly_shouldReturnFilteredReviews() {
        Pageable pageable = PageRequest.of(0, 10);
        String companyName = "Incognito Corp";
        Page<Review> reviewPage = new PageImpl<>(Collections.singletonList(review), pageable, 1);

        when(reviewRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(reviewPage);

        Page<ReviewResponse> result = reviewService.getReviews(companyName, null, pageable);

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertEquals(companyName, result.getContent().get(0).companyName());
        verify(reviewRepository).findAll(any(Specification.class), eq(pageable)); // Changed from argThat
    }

    @Test
    @SuppressWarnings("unchecked")
    void getReviews_withReviewTypeFilterOnly_shouldReturnFilteredReviews() {
        Pageable pageable = PageRequest.of(0, 10);
        ReviewType reviewType = ReviewType.POSITIVE;
        Page<Review> reviewPage = new PageImpl<>(Collections.singletonList(review), pageable, 1);
        review.setReviewType(reviewType); // Ensure the base review matches the filter for this test

        when(reviewRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(reviewPage);

        Page<ReviewResponse> result = reviewService.getReviews(null, reviewType, pageable);

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertEquals(reviewType, result.getContent().get(0).reviewType());
        verify(reviewRepository).findAll(any(Specification.class), eq(pageable)); // Changed from argThat
    }

    @Test
    void incrementLikeCount_whenReviewExists_shouldIncrementAndReturnReview() {
        // Arrange
        Instant testInstant = Instant.now();
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
        reviewAfterLike.setCreatedAt(testInstant);
        reviewAfterLike.setUpdatedAt(testInstant);

        when(reviewRepository.incrementLikeCount(1L)).thenReturn(1); // Simulate successful DB update
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(reviewAfterLike)); // Return the review with updated count

        // Act
        ReviewResponse reviewResponse = reviewService.incrementLikeCount(1L);

        // Assert
        assertNotNull(reviewResponse);
        assertEquals(reviewAfterLike.getId(), reviewResponse.id());
        assertEquals(reviewAfterLike.getLikeCount(), reviewResponse.likeCount()); // Assert updated like count
        assertEquals(reviewAfterLike.getReviewerName(), reviewResponse.reviewerName());
        assertEquals(0, reviewResponse.totalComments()); // DTO mapping handles null from entity
        assertEquals(reviewAfterLike.getCreatedAt(), reviewResponse.createdAt());
        assertEquals(reviewAfterLike.getUpdatedAt(), reviewResponse.updatedAt());
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
        Instant testInstant = Instant.now();
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
        reviewAfterDislike.setCreatedAt(testInstant);
        reviewAfterDislike.setUpdatedAt(testInstant);

        when(reviewRepository.incrementDislikeCount(1L)).thenReturn(1); // Simulate successful DB update
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(reviewAfterDislike)); // Return the review with updated count

        // Act
        ReviewResponse reviewResponse = reviewService.incrementDislikeCount(1L);

        // Assert
        assertNotNull(reviewResponse);
        assertEquals(reviewAfterDislike.getId(), reviewResponse.id());
        assertEquals(reviewAfterDislike.getDislikeCount(), reviewResponse.dislikeCount()); // Assert updated dislike count
        assertEquals(reviewAfterDislike.getReviewerName(), reviewResponse.reviewerName());
        assertEquals(0, reviewResponse.totalComments()); // DTO mapping handles null from entity
        assertEquals(reviewAfterDislike.getCreatedAt(), reviewResponse.createdAt());
        assertEquals(reviewAfterDislike.getUpdatedAt(), reviewResponse.updatedAt());
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

    @Test
    void mapToReviewResponse_whenTotalCommentsIsNull_shouldReturnZeroInResponse() {
        // Arrange
        Review reviewWithNullComments = Review.builder()
                .id(1L)
                .reviewType(ReviewType.POSITIVE)
                .title("Test Title")
                .contentHtml("Test Content")
                .ipAddress("127.0.0.1")
                .likeCount(0)
                .dislikeCount(0)
                .hasComment(false)
                .status(ReviewStatus.PENDING)
                .reviewerName("Tester")
                .totalComments(null) // Explicitly set totalComments to null
                .build();
        reviewWithNullComments.setCreatedAt(Instant.now());
        reviewWithNullComments.setUpdatedAt(Instant.now());

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(reviewWithNullComments));

        // Act
        ReviewResponse reviewResponse = reviewService.getReviewById(1L);

        // Assert
        assertNotNull(reviewResponse);
        assertEquals(0, reviewResponse.totalComments(), "Total comments should be 0 when entity's totalComments is null");
    }
}
