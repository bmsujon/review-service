package com.incognito.reviewservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Data Transfer Object for creating a new comment on a review.")
public record CommentCreateRequest(
    @NotBlank(message = "Comment content cannot be blank")
    @Size(min = 1, max = 5000, message = "Comment content must be between 1 and 5000 characters")
    @Schema(description = "The content of the comment.", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 5000, example = "Thanks for the detailed review!")
    String content
    // You might add other fields here later, e.g., commenterId, commenterName, if not anonymous
) {
}