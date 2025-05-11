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
        ReviewResponse response = ReviewResponse.builder()
                .id(1L)
                .title("Title")
                .content("Content Test Content")
                .reviewType(ReviewType.POSITIVE)
                .companyName("Incognito Corp")
                .ipAddress("127.0.0.1")
                .isEmployee(true)
                .dept("Engineering")
                .role("Software Engineer")
                .website("http://incognito.com")
                .likeCount(0)
                .dislikeCount(0)
                .hasComment(false)
                .status(com.incognito.reviewservice.model.ReviewStatus.PENDING)
                .build();

        Mockito.when(reviewService.createReview(any(ReviewCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Title\",\"content\":\"Content Test Content\",\"reviewType\":\"POSITIVE\", \"companyName\":\"Incognito Corp\", \"ipAddress\":\"127.0.0.1\", \"isEmployee\":true, \"dept\":\"Engineering\", \"role\":\"Software Engineer\", \"website\":\"http://incognito.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.content").value("Content Test Content"))
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
        ReviewResponse reviewResponse = ReviewResponse.builder()
                .id(1L)
                .title("Test Title")
                .content("Test Content Sufficiently Long")
                .reviewType(ReviewType.NEGATIVE)
                .companyName("Incognito Corp")
                .status(com.incognito.reviewservice.model.ReviewStatus.APPROVED)
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
                .andExpect(jsonPath("$.content[0].content").value("Test Content Sufficiently Long"))
                .andExpect(jsonPath("$.content[0].reviewType").value("NEGATIVE"))
                .andExpect(jsonPath("$.content[0].companyName").value("Incognito Corp"))
                .andExpect(jsonPath("$.content[0].status").value("APPROVED"))
                .andExpect(jsonPath("$.content[0].likeCount").value(10))
                .andExpect(jsonPath("$.content[0].dislikeCount").value(1));
    }

    @Test
    void testGetReviewById() throws Exception {
        Long reviewId = 1L;
        ReviewResponse response = ReviewResponse.builder()
                .id(reviewId)
                .title("Test Title")
                .content("Test Content")
                .reviewType(ReviewType.POSITIVE)
                .companyName("Incognito Corp")
                .ipAddress("127.0.0.1")
                .isEmployee(false)
                .dept("Sales")
                .role("Manager")
                .website("http://example.com")
                .likeCount(5)
                .dislikeCount(0)
                .hasComment(true)
                .status(com.incognito.reviewservice.model.ReviewStatus.REJECTED)
                .build();

        Mockito.when(reviewService.getReviewById(eq(reviewId))).thenReturn(response);

        mockMvc.perform(get("/api/v1/reviews/{id}", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.content").value("Test Content"))
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
        ReviewResponse response = ReviewResponse.builder()
                .id(reviewId)
                .title("Test Title")
                .content("Test Content")
                .reviewType(ReviewType.POSITIVE)
                .likeCount(1)
                .build();

        Mockito.when(reviewService.incrementLikeCount(eq(reviewId))).thenReturn(response);

        mockMvc.perform(put("/api/v1/reviews/{reviewId}/like", reviewId))
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

        mockMvc.perform(put("/api/v1/reviews/{reviewId}/dislike", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.dislikeCount").value(1));
    }
}