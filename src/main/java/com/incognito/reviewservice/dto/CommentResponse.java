package com.incognito.reviewservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "Data Transfer Object representing a comment.")
public class CommentResponse {

    @Schema(description = "Unique identifier of the comment.", example = "101")
    private Long id;

    @Schema(description = "The content of the comment.", example = "Great insights!")
    private String content;

    @Schema(description = "Number of likes the comment has received.", example = "5")
    private Integer likeCount;

    @Schema(description = "Number of dislikes the comment has received.", example = "0")
    private Integer dislikeCount;

    // @Schema(description = "Current status of the comment.", example = "ACTIVE")
    // private CommentStatus status; // If you have a CommentStatus enum

    @Schema(description = "ID of the review this comment belongs to.", example = "1")
    private Long reviewId;

    @Schema(description = "ID of the parent comment if this is a reply, null otherwise.", example = "100", nullable = true)
    private Long parentId; // New field

    @Schema(description = "Timestamp of when the comment was created.", example = "2023-10-28T10:30:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp of when the comment was last updated.", example = "2023-10-28T11:00:00Z")
    private Instant updatedAt;

    // Add other fields like commenterInfo if needed
}