package com.incognito.reviewservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incognito.reviewservice.dto.CommentCreateRequest;
import com.incognito.reviewservice.dto.CommentResponse;
import com.incognito.reviewservice.exception.BadRequestException;
import com.incognito.reviewservice.exception.ResourceNotFoundException;
import com.incognito.reviewservice.model.CommentStatus;
import com.incognito.reviewservice.service.CommentService;
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

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@Import(CommentControllerTest.CommentControllerTestConfig.class)
class CommentControllerTest {

    @TestConfiguration
    static class CommentControllerTestConfig {
        @Bean
        public CommentService commentService() {
            return Mockito.mock(CommentService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    private CommentCreateRequest commentCreateRequest;
    private CommentResponse commentResponse;
    private Long testReviewId = 1L;
    private Long testCommentId = 101L;
    private Long testParentId = 50L;

    @BeforeEach
    void setUp() {
        Mockito.reset(commentService); // Reset mock before each test
        commentCreateRequest = new CommentCreateRequest(
                "This is a test comment.",
                "TestUser"
        );

        commentResponse = new CommentResponse(
                testCommentId,
                "This is a test comment.",
                0,
                0,
                testReviewId,
                null, // parentId is null for a top-level comment response example
                Instant.now(),
                Instant.now(),
                CommentStatus.ACTIVE,
                false,
                "TestUser",
                0
        );
    }

    @Test
    void testCreateComment_TopLevel_Success() throws Exception {
        given(commentService.createComment(eq(testReviewId), eq(null), any(CommentCreateRequest.class)))
                .willReturn(commentResponse);

        mockMvc.perform(post("/api/v1/reviews/{reviewId}/comments", testReviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/reviews/" + testReviewId + "/comments/" + testCommentId)))
                .andExpect(jsonPath("$.id", is(testCommentId.intValue())))
                .andExpect(jsonPath("$.content", is(commentCreateRequest.content())));

        verify(commentService).createComment(eq(testReviewId), eq(null), any(CommentCreateRequest.class));
    }

    @Test
    void testCreateComment_AsReply_Success() throws Exception {
        CommentResponse replyResponse = new CommentResponse(
                testCommentId + 1, "This is a reply", 0, 0, testReviewId, testParentId,
                Instant.now(), Instant.now(), CommentStatus.ACTIVE, false, "ReplyUser", 0
        );
        given(commentService.createComment(eq(testReviewId), eq(testParentId), any(CommentCreateRequest.class)))
                .willReturn(replyResponse);

        mockMvc.perform(post("/api/v1/reviews/{reviewId}/comments", testReviewId)
                        .param("parentId", String.valueOf(testParentId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/reviews/" + testReviewId + "/comments/" + (testCommentId + 1))))
                .andExpect(jsonPath("$.parentId", is(testParentId.intValue())));

        verify(commentService).createComment(eq(testReviewId), eq(testParentId), any(CommentCreateRequest.class));
    }

    @Test
    void testCreateComment_ValidationFailure() throws Exception {
        CommentCreateRequest invalidRequest = new CommentCreateRequest("", null); // Blank content

        mockMvc.perform(post("/api/v1/reviews/{reviewId}/comments", testReviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateComment_ReviewNotFound() throws Exception {
        given(commentService.createComment(eq(testReviewId), eq(null), any(CommentCreateRequest.class)))
                .willThrow(new ResourceNotFoundException("Review not found"));

        mockMvc.perform(post("/api/v1/reviews/{reviewId}/comments", testReviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateComment_ParentNotFound() throws Exception {
        given(commentService.createComment(eq(testReviewId), eq(testParentId), any(CommentCreateRequest.class)))
                .willThrow(new ResourceNotFoundException("Parent comment not found"));

        mockMvc.perform(post("/api/v1/reviews/{reviewId}/comments", testReviewId)
                        .param("parentId", String.valueOf(testParentId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateComment_ParentBelongsToDifferentReview() throws Exception {
        given(commentService.createComment(eq(testReviewId), eq(testParentId), any(CommentCreateRequest.class)))
                .willThrow(new BadRequestException("Parent comment does not belong to review"));

        mockMvc.perform(post("/api/v1/reviews/{reviewId}/comments", testReviewId)
                        .param("parentId", String.valueOf(testParentId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCommentsByReviewId_Success() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        List<CommentResponse> commentList = Collections.singletonList(commentResponse);
        Page<CommentResponse> commentPage = new PageImpl<>(commentList, pageable, commentList.size());

        given(commentService.getCommentsByReviewId(eq(testReviewId), any(Pageable.class))).willReturn(commentPage);

        mockMvc.perform(get("/api/v1/reviews/{reviewId}/comments", testReviewId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(testCommentId.intValue())));

        verify(commentService).getCommentsByReviewId(eq(testReviewId), any(Pageable.class));
    }

    @Test
    void testGetCommentsByReviewId_ReviewNotFound() throws Exception {
        given(commentService.getCommentsByReviewId(eq(testReviewId), any(Pageable.class)))
                .willThrow(new ResourceNotFoundException("Review not found"));

        mockMvc.perform(get("/api/v1/reviews/{reviewId}/comments", testReviewId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testLikeComment_Success() throws Exception {
        CommentResponse likedResponse = new CommentResponse(
                testCommentId, "Content", commentResponse.likeCount() + 1, 0, testReviewId, null,
                Instant.now(), Instant.now(), CommentStatus.ACTIVE, false, "User", 0
        );
        given(commentService.incrementLikeCount(testReviewId, testCommentId)).willReturn(likedResponse);

        mockMvc.perform(put("/api/v1/reviews/{reviewId}/comments/{commentId}/like", testReviewId, testCommentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likeCount", is(likedResponse.likeCount())));

        verify(commentService).incrementLikeCount(testReviewId, testCommentId);
    }

    @Test
    void testLikeComment_NotFound() throws Exception {
        given(commentService.incrementLikeCount(testReviewId, testCommentId))
                .willThrow(new ResourceNotFoundException("Comment not found"));

        mockMvc.perform(put("/api/v1/reviews/{reviewId}/comments/{commentId}/like", testReviewId, testCommentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDislikeComment_Success() throws Exception {
        CommentResponse dislikedResponse = new CommentResponse(
                testCommentId, "Content", 0, commentResponse.dislikeCount() + 1, testReviewId, null,
                Instant.now(), Instant.now(), CommentStatus.ACTIVE, false, "User", 0
        );
        given(commentService.incrementDislikeCount(testReviewId, testCommentId)).willReturn(dislikedResponse);

        mockMvc.perform(put("/api/v1/reviews/{reviewId}/comments/{commentId}/dislike", testReviewId, testCommentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dislikeCount", is(dislikedResponse.dislikeCount())));

        verify(commentService).incrementDislikeCount(testReviewId, testCommentId);
    }

    @Test
    void testDislikeComment_NotFound() throws Exception {
        given(commentService.incrementDislikeCount(testReviewId, testCommentId))
                .willThrow(new ResourceNotFoundException("Comment not found"));

        mockMvc.perform(put("/api/v1/reviews/{reviewId}/comments/{commentId}/dislike", testReviewId, testCommentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetRepliesOfComment_Success() throws Exception {
        Pageable pageable = PageRequest.of(0, 5);
        CommentResponse reply = new CommentResponse(
                testCommentId + 1, "A reply", 0, 0, testReviewId, testCommentId, Instant.now(), Instant.now(),
                CommentStatus.ACTIVE, false, "ReplyUser", 0
        );
        List<CommentResponse> repliesList = Collections.singletonList(reply);
        Page<CommentResponse> repliesPage = new PageImpl<>(repliesList, pageable, repliesList.size());

        given(commentService.getRepliesOfComment(eq(testReviewId), eq(testCommentId), any(Pageable.class)))
                .willReturn(repliesPage);

        mockMvc.perform(get("/api/v1/reviews/{reviewId}/comments/{commentId}/replies", testReviewId, testCommentId)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].parentId", is(testCommentId.intValue())));

        verify(commentService).getRepliesOfComment(eq(testReviewId), eq(testCommentId), any(Pageable.class));
    }

    @Test
    void testGetRepliesOfComment_ReviewNotFound() throws Exception {
        given(commentService.getRepliesOfComment(eq(testReviewId), eq(testCommentId), any(Pageable.class)))
                .willThrow(new ResourceNotFoundException("Review not found"));

        mockMvc.perform(get("/api/v1/reviews/{reviewId}/comments/{commentId}/replies", testReviewId, testCommentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetRepliesOfComment_ParentCommentNotFound() throws Exception {
        given(commentService.getRepliesOfComment(eq(testReviewId), eq(testCommentId), any(Pageable.class)))
                .willThrow(new ResourceNotFoundException("Parent comment not found"));

        mockMvc.perform(get("/api/v1/reviews/{reviewId}/comments/{commentId}/replies", testReviewId, testCommentId))
                .andExpect(status().isNotFound());
    }
}