package com.incognito.reviewservice.controller;

import com.incognito.reviewservice.dto.CommentCreateRequest;
import com.incognito.reviewservice.dto.CommentResponse;
import com.incognito.reviewservice.service.CommentService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(CommentController.class) // This will instantiate CommentController and inject mocks
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean // Use @Mock for the service
    private CommentService commentService;

    // If CommentController is not automatically picking up the mock,
    // you might need @InjectMocks on an instance of CommentController,
    // but @WebMvcTest usually handles this if the controller has a proper constructor.

    @Test
    void testCreateComment() throws Exception {
        CommentResponse response = new CommentResponse(
                1L,
                "Test Comment",
                0,
                0,
                1L,
                null, // parentId
                null, // createdAt
                null, // updatedAt
                null  // status
        );

        Mockito.when(commentService.createComment(eq(1L), isNull(), any(CommentCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/reviews/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Test Comment\"}")
                        .param("parentId", (String) null))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Test Comment"))
                .andExpect(jsonPath("$.reviewId").value(1L));
    }

    @Test
    void testGetCommentsByReviewId() throws Exception {
        CommentResponse commentResponse = new CommentResponse(
                1L,
                "Test Comment",
                5,
                0,
                1L,
                null, // parentId
                null, // createdAt
                null, // updatedAt
                null  // status
        );

        Mockito.when(commentService.getCommentsByReviewId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(commentResponse), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/reviews/1/comments")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].content").value("Test Comment"))
                .andExpect(jsonPath("$.content[0].reviewId").value(1L));
    }

    @Test
    void testLikeComment() throws Exception {
        Long reviewId = 1L;
        Long commentId = 1L;
        CommentResponse response = new CommentResponse(
                commentId,
                "Test Comment",
                1, // likeCount
                0, // dislikeCount
                reviewId,
                null, // parentId
                null, // createdAt
                null, // updatedAt
                null  // status
        );

        Mockito.when(commentService.incrementLikeCount(eq(reviewId), eq(commentId))).thenReturn(response);

        mockMvc.perform(put("/api/v1/reviews/{reviewId}/comments/{commentId}/like", reviewId, commentId)) // Changed from post to put
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.likeCount").value(1));
    }

    @Test
    void testDislikeComment() throws Exception {
        Long reviewId = 1L;
        Long commentId = 1L;
        CommentResponse response = new CommentResponse(
                commentId,
                "Test Comment",
                0, // likeCount
                1, // dislikeCount
                reviewId,
                null, // parentId
                null, // createdAt
                null, // updatedAt
                null  // status
        );

        Mockito.when(commentService.incrementDislikeCount(eq(reviewId), eq(commentId))).thenReturn(response);

        mockMvc.perform(put("/api/v1/reviews/{reviewId}/comments/{commentId}/dislike", reviewId, commentId)) // Changed from post to put
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.dislikeCount").value(1));
    }
}