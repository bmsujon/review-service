package com.incognito.reviewservice.controller;

import com.incognito.reviewservice.dto.CommentCreateRequest;
import com.incognito.reviewservice.dto.CommentResponse;
import com.incognito.reviewservice.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/reviews/{reviewId}/comments")
@Tag(name = "Comment API", description = "APIs for managing comments on reviews")
@Slf4j
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "Create a new comment for a review", description = "Creates a new comment for a specific review. Can be a top-level comment or a reply to an existing comment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comment created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or review/parent comment not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "404", description = "Review or Parent Comment not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class)))
    })
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @Parameter(description = "ID of the review to which the comment belongs", required = true, example = "1")
            @PathVariable Long reviewId,
            @Parameter(description = "ID of the parent comment if this is a reply. Omit for top-level comments.", example = "10")
            @RequestParam(required = false) Long parentId,
            @Valid @RequestBody CommentCreateRequest request) {
        CommentResponse commentResponse = commentService.createComment(reviewId, parentId, request);
        // Construct URI for the newly created comment
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest() // Starts with /api/v1/reviews/{reviewId}/comments
                .path("/{commentId}") // Appends the comment ID
                .buildAndExpand(commentResponse.id()) // Populates {commentId}
                .toUri();
        log.info("commentResponse: {}", commentResponse);
        return ResponseEntity.created(location).body(commentResponse);
    }

    @Operation(summary = "Get top level comments for a review", description = "Retrieves a paginated list of top level comments for a specific review.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved comments",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Page.class))), // Page of CommentResponse
            @ApiResponse(responseCode = "404", description = "Review not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class)))
    })
    @GetMapping
    public ResponseEntity<Page<CommentResponse>> getCommentsByReviewId(
            @Parameter(description = "ID of the review whose comments are to be retrieved", required = true, example = "1")
            @PathVariable Long reviewId,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {
        Page<CommentResponse> commentPage = commentService.getCommentsByReviewId(reviewId, pageable);
        return ResponseEntity.ok(commentPage);
    }

    @Operation(summary = "Increment the like count of a comment", description = "Increments the like counter for the specified comment by one.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Like count incremented successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Comment not found or does not belong to the review",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class)))
    })
    @PutMapping("/{commentId}/like")
    public ResponseEntity<CommentResponse> likeComment(
            @Parameter(description = "ID of the review", required = true, example = "1")
            @PathVariable Long reviewId,
            @Parameter(description = "ID of the comment to be liked", required = true, example = "101")
            @PathVariable Long commentId) {
        CommentResponse updatedComment = commentService.incrementLikeCount(reviewId, commentId);
        return ResponseEntity.ok(updatedComment);
    }

    @Operation(summary = "Increment the dislike count of a comment", description = "Increments the dislike counter for the specified comment by one.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dislike count incremented successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Comment not found or does not belong to the review",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class)))
    })
    @PutMapping("/{commentId}/dislike")
    public ResponseEntity<CommentResponse> dislikeComment(
            @Parameter(description = "ID of the review", required = true, example = "1")
            @PathVariable Long reviewId,
            @Parameter(description = "ID of the comment to be disliked", required = true, example = "101")
            @PathVariable Long commentId) {
        CommentResponse updatedComment = commentService.incrementDislikeCount(reviewId, commentId);
        return ResponseEntity.ok(updatedComment);
    }

    //Let's make another api which will return paginated list of replies of a comment
    @Operation(summary = "Get replies for a comment", description = "Retrieves a paginated list of replies for a specific comment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved replies",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Page.class))), // Page of CommentResponse
            @ApiResponse(responseCode = "404", description = "Comment not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class)))
    })
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<Page<CommentResponse>> getRepliesOfComment(
            @Parameter(description = "ID of the review", required = true, example = "1")
            @PathVariable Long reviewId,
            @Parameter(description = "ID of the comment whose replies are to be retrieved", required = true, example = "101")
            @PathVariable Long commentId,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {
        Page<CommentResponse> replyPage = commentService.getRepliesOfComment(reviewId, commentId, pageable);
        return ResponseEntity.ok(replyPage);
    }
    // Add other controller methods here (e.g., getCommentById, updateComment, deleteComment, likeComment, dislikeComment)
}