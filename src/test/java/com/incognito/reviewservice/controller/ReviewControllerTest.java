package com.incognito.reviewservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incognito.reviewservice.dto.ReviewCreateRequest;
import com.incognito.reviewservice.dto.ReviewResponse;
import com.incognito.reviewservice.exception.ResourceNotFoundException;
import com.incognito.reviewservice.model.ReviewStatus;
import com.incognito.reviewservice.model.ReviewType;
import com.incognito.reviewservice.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@Import(ReviewControllerTest.ReviewControllerTestConfig.class)
class ReviewControllerTest {

    @TestConfiguration
    static class ReviewControllerTestConfig {
        @Bean
        public ReviewService reviewService() {
            return Mockito.mock(ReviewService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    private ReviewCreateRequest reviewCreateRequest;
    private ReviewResponse reviewResponse;

    @BeforeEach
    void setUp() {
        reviewCreateRequest = new ReviewCreateRequest(
                ReviewType.POSITIVE,
                "Great Place to Work",
                "<p>Amazing culture and benefits.</p>",
                "192.168.1.100",
                "Engineering",
                "Software Developer",
                "Tech Solutions Inc.",
                "http://techsolutions.example.com",
                true,
                Instant.parse("2022-01-01T00:00:00Z"),
                Instant.parse("2023-01-01T00:00:00Z"),
                "John Doe"
        );

        reviewResponse = new ReviewResponse(
                1L,
                ReviewType.POSITIVE,
                "Great Place to Work",
                "<p>Amazing culture and benefits.</p>",
                "192.168.1.100",
                0,
                0,
                false,
                ReviewStatus.PENDING,
                true,
                "Engineering",
                "Software Developer",
                "Tech Solutions Inc.",
                "http://techsolutions.example.com",
                Instant.parse("2022-01-01T00:00:00Z"),
                Instant.parse("2023-01-01T00:00:00Z"),
                Instant.now(),
                Instant.now(),
                "John Doe",
                0
        );
    }

    @Test
    void testCreateReview_Success() throws Exception {
        // Given
        given(reviewService.createReview(any(ReviewCreateRequest.class))).willReturn(reviewResponse);

        // When
        ResultActions resultActions = mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewCreateRequest)));

        // Then
        resultActions.andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andDo(mvcResult -> {
                    String locationHeader = mvcResult.getResponse().getHeader("Location");
                    assertNotNull(locationHeader, "Location header should not be null");
                    java.net.URI locationUri = new java.net.URI(locationHeader);
                    assertEquals("/api/v1/reviews/" + reviewResponse.id(), locationUri.getPath(), "Location URI path mismatch");
                })
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(reviewResponse.id().intValue())))
                .andExpect(jsonPath("$.title", is(reviewResponse.title())))
                .andExpect(jsonPath("$.reviewerName", is(reviewResponse.reviewerName())));

        verify(reviewService).createReview(any(ReviewCreateRequest.class));
    }

    @Test
    void testCreateReview_ValidationFailure() throws Exception {
        // Given
        ReviewCreateRequest invalidRequest = new ReviewCreateRequest(
                null,
                "",
                "<p>Content</p>",
                "127.0.0.1", null, null, null, null, false, null, null, null);

        // When
        ResultActions resultActions = mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        // Then
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    void testGetReviewById_Success() throws Exception {
        // Given
        Long reviewId = 1L;
        given(reviewService.getReviewById(reviewId)).willReturn(reviewResponse);

        // When
        ResultActions resultActions = mockMvc.perform(get("/api/v1/reviews/{id}", reviewId));

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(reviewResponse.id().intValue())))
                .andExpect(jsonPath("$.title", is(reviewResponse.title())));

        verify(reviewService).getReviewById(reviewId);
    }

    @Test
    void testGetReviewById_NotFound() throws Exception {
        // Given
        Long reviewId = 99L;
        given(reviewService.getReviewById(reviewId)).willThrow(new ResourceNotFoundException("Review not found"));

        // When
        ResultActions resultActions = mockMvc.perform(get("/api/v1/reviews/{id}", reviewId));

        // Then
        resultActions.andExpect(status().isNotFound());
        verify(reviewService).getReviewById(reviewId);
    }

    @Test
    void testGetReviews_Success_NoFilters() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<ReviewResponse> reviewList = Collections.singletonList(reviewResponse);
        Page<ReviewResponse> reviewPage = new PageImpl<>(reviewList, pageable, reviewList.size());

        given(reviewService.getReviews(eq(null), eq(null), any(Pageable.class))).willReturn(reviewPage);

        // When
        ResultActions resultActions = mockMvc.perform(get("/api/v1/reviews")
                .param("page", "0")
                .param("size", "10"));

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(reviewResponse.id().intValue())))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.totalElements", is(1)));

        verify(reviewService).getReviews(eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void testGetReviews_Success_WithFilters() throws Exception {
        // Given
        String companyName = "Tech Solutions Inc.";
        ReviewType reviewType = ReviewType.POSITIVE;
        Pageable pageable = PageRequest.of(0, 5);
        List<ReviewResponse> reviewList = Collections.singletonList(reviewResponse);
        Page<ReviewResponse> reviewPage = new PageImpl<>(reviewList, pageable, reviewList.size());

        given(reviewService.getReviews(eq(companyName), eq(reviewType), any(Pageable.class))).willReturn(reviewPage);

        // When
        ResultActions resultActions = mockMvc.perform(get("/api/v1/reviews")
                .param("companyName", companyName)
                .param("reviewType", reviewType.toString())
                .param("page", "0")
                .param("size", "5"));

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].companyName", is(companyName)));

        verify(reviewService).getReviews(eq(companyName), eq(reviewType), any(Pageable.class));
    }

    @Test
    void testLikeReview_Success() throws Exception {
        // Given
        Long reviewId = 1L;
        ReviewResponse likedResponse = new ReviewResponse(
            reviewResponse.id(), reviewResponse.reviewType(), reviewResponse.title(), reviewResponse.contentHtml(),
            reviewResponse.ipAddress(), reviewResponse.likeCount() + 1, reviewResponse.dislikeCount(),
            reviewResponse.hasComment(), reviewResponse.status(), reviewResponse.isEmployee(), reviewResponse.dept(),
            reviewResponse.role(), reviewResponse.companyName(), reviewResponse.website(), reviewResponse.workStartDate(),
            reviewResponse.workEndDate(), reviewResponse.createdAt(), reviewResponse.updatedAt(), reviewResponse.reviewerName(),
            reviewResponse.totalComments()
        );
        given(reviewService.incrementLikeCount(reviewId)).willReturn(likedResponse);

        // When
        ResultActions resultActions = mockMvc.perform(put("/api/v1/reviews/{reviewId}/like", reviewId));

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(reviewId.intValue())))
                .andExpect(jsonPath("$.likeCount", is(likedResponse.likeCount())));

        verify(reviewService).incrementLikeCount(reviewId);
    }

    @Test
    void testLikeReview_NotFound() throws Exception {
        // Given
        Long reviewId = 99L;
        given(reviewService.incrementLikeCount(reviewId)).willThrow(new ResourceNotFoundException("Review not found"));

        // When
        ResultActions resultActions = mockMvc.perform(put("/api/v1/reviews/{reviewId}/like", reviewId));

        // Then
        resultActions.andExpect(status().isNotFound());
        verify(reviewService).incrementLikeCount(reviewId);
    }

    @Test
    void testDislikeReview_Success() throws Exception {
        // Given
        Long reviewId = 1L;
         ReviewResponse dislikedResponse = new ReviewResponse(
            reviewResponse.id(), reviewResponse.reviewType(), reviewResponse.title(), reviewResponse.contentHtml(),
            reviewResponse.ipAddress(), reviewResponse.likeCount(), reviewResponse.dislikeCount() + 1,
            reviewResponse.hasComment(), reviewResponse.status(), reviewResponse.isEmployee(), reviewResponse.dept(),
            reviewResponse.role(), reviewResponse.companyName(), reviewResponse.website(), reviewResponse.workStartDate(),
            reviewResponse.workEndDate(), reviewResponse.createdAt(), reviewResponse.updatedAt(), reviewResponse.reviewerName(),
            reviewResponse.totalComments()
        );
        given(reviewService.incrementDislikeCount(reviewId)).willReturn(dislikedResponse);

        // When
        ResultActions resultActions = mockMvc.perform(put("/api/v1/reviews/{reviewId}/dislike", reviewId));

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(reviewId.intValue())))
                .andExpect(jsonPath("$.dislikeCount", is(dislikedResponse.dislikeCount())));

        verify(reviewService).incrementDislikeCount(reviewId);
    }

    @Test
    void testDislikeReview_NotFound() throws Exception {
        // Given
        Long reviewId = 99L;
        given(reviewService.incrementDislikeCount(reviewId)).willThrow(new ResourceNotFoundException("Review not found"));

        // When
        ResultActions resultActions = mockMvc.perform(put("/api/v1/reviews/{reviewId}/dislike", reviewId));

        // Then
        resultActions.andExpect(status().isNotFound());
        verify(reviewService).incrementDislikeCount(reviewId);
    }
}