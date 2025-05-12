package com.incognito.reviewservice.dto;

import com.incognito.reviewservice.model.CommentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Data Transfer Object representing a comment.")
public record CommentResponse(
    @Schema(description = "Unique identifier of the comment.", example = "101")
    Long id,

    @Schema(description = "The content of the comment.", example = "Great insights!")
    String content,

    @Schema(description = "Number of likes the comment has received.", example = "5")
    Integer likeCount,

    @Schema(description = "Number of dislikes the comment has received.", example = "0")
    Integer dislikeCount,

    @Schema(description = "ID of the review this comment belongs to.", example = "1")
    Long reviewId,

    @Schema(description = "ID of the parent comment if this is a reply, null otherwise.", example = "100", nullable = true)
    Long parentId, // New field

    @Schema(description = "Timestamp of when the comment was created.", example = "2023-10-28T10:30:00Z")
    Instant createdAt,

    @Schema(description = "Timestamp of when the comment was last updated.", example = "2023-10-28T11:00:00Z")
    Instant updatedAt,

    @Schema(description = "Status of the comment.", example = "ACTIVE")
    CommentStatus status
    // Add other fields like commenterInfo if needed
) {
}