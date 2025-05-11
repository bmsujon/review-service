package com.incognito.reviewservice.controller;

import com.incognito.reviewservice.dto.ReviewCreateRequest;
import com.incognito.reviewservice.dto.ReviewResponse;
import com.incognito.reviewservice.model.ReviewType;
import com.incognito.reviewservice.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @Test
    void testCreateReview() throws Exception {
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .title("Title")
                .content("Content Test Content")
                .reviewType(ReviewType.POSITIVE)
                .build();
        ReviewResponse response = ReviewResponse.builder()
                .id(1L)
                .title("Title")
                .content("Content Test Content")
                .reviewType(ReviewType.POSITIVE)
                .likeCount(0)
                .dislikeCount(0)
                .build();

        Mockito.when(reviewService.createReview(any(ReviewCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Title\",\"content\":\"Content Test Content\",\"reviewType\":\"POSITIVE\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.reviewType").value("POSITIVE"));
    }

    @Test
    void testGetReviews() throws Exception {
        ReviewResponse reviewResponse = ReviewResponse.builder()
                .id(1L)
                .title("Test Title")
                .content("Test Content Sufficiently Long")
                .reviewType(ReviewType.NEGATIVE)
                .likeCount(10)
                .dislikeCount(1)
                .build();

        Mockito.when(reviewService.getReviews(isNull(String.class), isNull(ReviewType.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(reviewResponse), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/reviews")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Test Title"))
                .andExpect(jsonPath("$.content[0].reviewType").value("NEGATIVE"));
    }

    @Test
    void testGetReviewById() throws Exception {
        Long reviewId = 1L;
        ReviewResponse response = ReviewResponse.builder()
                .id(reviewId)
                .title("Test Title")
                .content("Test Content")
                .reviewType(ReviewType.POSITIVE)
                .build();

        Mockito.when(reviewService.getReviewById(eq(reviewId))).thenReturn(response);

        mockMvc.perform(get("/api/v1/reviews/{id}", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.title").value("Test Title"));
    }

    @Test
    void testLikeReview() throws Exception {
        Long reviewId = 1L;
        ReviewResponse response = ReviewResponse.builder()
                .id(reviewId)
                .title("Test Title")
                .content("Test Content")
                .reviewType(ReviewType.POSITIVE)
                .likeCount(1)
                .build();

        Mockito.when(reviewService.incrementLikeCount(eq(reviewId))).thenReturn(response);

        mockMvc.perform(post("/api/v1/reviews/{reviewId}/like", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.likeCount").value(1));
    }

    @Test
    void testDislikeReview() throws Exception {
        Long reviewId = 1L;
        ReviewResponse response = ReviewResponse.builder()
                .id(reviewId)
                .title("Test Title")
                .content("Test Content")
                .reviewType(ReviewType.POSITIVE)
                .dislikeCount(1)
                .build();

        Mockito.when(reviewService.incrementDislikeCount(eq(reviewId))).thenReturn(response);

        mockMvc.perform(post("/api/v1/reviews/{reviewId}/dislike", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.dislikeCount").value(1));
    }
}