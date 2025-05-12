package com.incognito.reviewservice.controller;

import com.incognito.reviewservice.dto.ReviewCreateRequest;
import com.incognito.reviewservice.dto.ReviewResponse;
import com.incognito.reviewservice.model.ReviewType;
import com.incognito.reviewservice.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        ReviewResponse response = new ReviewResponse(
                1L,
                ReviewType.POSITIVE,
                "Title",
                "Content Test Content",
                "127.0.0.1",
                0,
                0,
                false,
                com.incognito.reviewservice.model.ReviewStatus.PENDING,
                true,
                "Engineering",
                "Software Engineer",
                "Incognito Corp",
                "http://incognito.com",
                null, // workStartDate
                null, // workEndDate
                null, // createdAt
                null // updatedAt
        );

        Mockito.when(reviewService.createReview(any(ReviewCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Title\",\"content\":\"Content Test Content\",\"reviewType\":\"POSITIVE\", \"companyName\":\"Incognito Corp\", \"ipAddress\":\"127.0.0.1\", \"isEmployee\":true, \"dept\":\"Engineering\", \"role\":\"Software Engineer\", \"website\":\"http://incognito.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.contentHtml").value("Content Test Content"))
                .andExpect(jsonPath("$.reviewType").value("POSITIVE"))
                .andExpect(jsonPath("$.companyName").value("Incognito Corp"))
                .andExpect(jsonPath("$.ipAddress").value("127.0.0.1"))
                .andExpect(jsonPath("$.isEmployee").value(true))
                .andExpect(jsonPath("$.dept").value("Engineering"))
                .andExpect(jsonPath("$.role").value("Software Engineer"))
                .andExpect(jsonPath("$.website").value("http://incognito.com"))
                .andExpect(jsonPath("$.likeCount").value(0))
                .andExpect(jsonPath("$.dislikeCount").value(0))
                .andExpect(jsonPath("$.hasComment").value(false))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testGetReviews() throws Exception {
        ReviewResponse reviewResponse = new ReviewResponse(
                1L,
                ReviewType.NEGATIVE,
                "Test Title",
                "Test Content Sufficiently Long",
                null, // ipAddress
                10,
                1,
                null, // hasComment
                com.incognito.reviewservice.model.ReviewStatus.APPROVED,
                null, // isEmployee
                null, // dept
                null, // role
                "Incognito Corp",
                null, // website
                null, // workStartDate
                null, // workEndDate
                null, // createdAt
                null // updatedAt
        );

        Mockito.when(reviewService.getReviews(isNull(String.class), isNull(ReviewType.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(reviewResponse), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/reviews")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Test Title"))
                .andExpect(jsonPath("$.content[0].contentHtml").value("Test Content Sufficiently Long"))
                .andExpect(jsonPath("$.content[0].reviewType").value("NEGATIVE"))
                .andExpect(jsonPath("$.content[0].companyName").value("Incognito Corp"))
                .andExpect(jsonPath("$.content[0].status").value("APPROVED"))
                .andExpect(jsonPath("$.content[0].likeCount").value(10))
                .andExpect(jsonPath("$.content[0].dislikeCount").value(1));
    }

    @Test
    void testGetReviewById() throws Exception {
        Long reviewId = 1L;
        ReviewResponse response = new ReviewResponse(
                reviewId,
                ReviewType.POSITIVE,
                "Test Title",
                "Test Content",
                "127.0.0.1",
                5,
                0,
                true,
                com.incognito.reviewservice.model.ReviewStatus.REJECTED,
                false,
                "Sales",
                "Manager",
                "Incognito Corp",
                "http://example.com",
                null, // workStartDate
                null, // workEndDate
                null, // createdAt
                null // updatedAt
        );

        Mockito.when(reviewService.getReviewById(eq(reviewId))).thenReturn(response);

        mockMvc.perform(get("/api/v1/reviews/{id}", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.contentHtml").value("Test Content"))
                .andExpect(jsonPath("$.reviewType").value("POSITIVE"))
                .andExpect(jsonPath("$.companyName").value("Incognito Corp"))
                .andExpect(jsonPath("$.ipAddress").value("127.0.0.1"))
                .andExpect(jsonPath("$.isEmployee").value(false))
                .andExpect(jsonPath("$.dept").value("Sales"))
                .andExpect(jsonPath("$.role").value("Manager"))
                .andExpect(jsonPath("$.website").value("http://example.com"))
                .andExpect(jsonPath("$.likeCount").value(5))
                .andExpect(jsonPath("$.dislikeCount").value(0))
                .andExpect(jsonPath("$.hasComment").value(true))
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void testLikeReview() throws Exception {
        Long reviewId = 1L;
        ReviewResponse response = new ReviewResponse(
                reviewId,
                ReviewType.POSITIVE,
                "Test Title",
                "Test Content",
                null, // ipAddress
                1, // likeCount
                0, // dislikeCount
                null, // hasComment
                null, // status
                null, // isEmployee
                null, // dept
                null, // role
                null, // companyName
                null, // website
                null, // workStartDate
                null, // workEndDate
                null, // createdAt
                null // updatedAt
        );

        Mockito.when(reviewService.incrementLikeCount(eq(reviewId))).thenReturn(response);

        mockMvc.perform(put("/api/v1/reviews/{reviewId}/like", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.likeCount").value(1));
    }

    @Test
    void testDislikeReview() throws Exception {
        Long reviewId = 1L;
        ReviewResponse response = new ReviewResponse(
                reviewId,
                ReviewType.POSITIVE,
                "Test Title",
                "Test Content",
                null, // ipAddress
                0, // likeCount
                1, // dislikeCount
                null, // hasComment
                null, // status
                null, // isEmployee
                null, // dept
                null, // role
                null, // companyName
                null, // website
                null, // workStartDate
                null, // workEndDate
                null, // createdAt
                null // updatedAt
        );

        Mockito.when(reviewService.incrementDislikeCount(eq(reviewId))).thenReturn(response);

        mockMvc.perform(put("/api/v1/reviews/{reviewId}/dislike", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.dislikeCount").value(1));
    }
}