package com.incognito.reviewservice.dto;

import com.incognito.reviewservice.model.ReviewStatus;
import com.incognito.reviewservice.model.ReviewType;
import io.swagger.v3.oas.annotations.media.Schema; // Import
import java.time.Instant;

@Schema(description = "Data Transfer Object representing a review.")
public record ReviewResponse(
    @Schema(description = "Unique identifier of the review.", example = "1")
    Long id,

    @Schema(description = "Type of the review.", example = "COMPANY_REVIEW")
    ReviewType reviewType,

    @Schema(description = "Title of the review.", example = "Excellent Work Environment")
    String title,

    @Schema(description = "Main content/body of the review.", example = "The company offers great benefits and a supportive team.")
    String contentHtml,

    @Schema(description = "IP address of the user who submitted the review.", example = "192.168.1.100")
    String ipAddress,

    @Schema(description = "Number of likes the review has received.", example = "10")
    Integer likeCount,

    @Schema(description = "Number of dislikes the review has received.", example = "1")
    Integer dislikeCount,

    @Schema(description = "Indicates if the review has any comments.", example = "true")
    Boolean hasComment,

    @Schema(description = "Current status of the review (e.g., PENDING, APPROVED, REJECTED).", example = "APPROVED")
    ReviewStatus status,

    @Schema(description = "Indicates if the reviewer was an employee.", example = "true")
    Boolean isEmployee,

    @Schema(description = "Department of the reviewer, if applicable.", example = "Engineering")
    String dept,

    @Schema(description = "Role of the reviewer, if applicable.", example = "Software Developer")
    String role,

    @Schema(description = "Name of the company being reviewed.", example = "Incognito Tech")
    String companyName,

    @Schema(description = "Website URL related to the review.", example = "https://incognito.example.com")
    String website,

    @Schema(description = "The date when the work/experience started.", example = "2022-01-15T00:00:00Z")
    Instant workStartDate,

    @Schema(description = "The date when the work/experience ended.", example = "2023-01-15T00:00:00Z")
    Instant workEndDate,

    @Schema(description = "Timestamp of when the review was created.", example = "2023-10-27T10:30:00Z")
    Instant createdAt,

    @Schema(description = "Timestamp of when the review was last updated.", example = "2023-10-27T11:00:00Z")
    Instant updatedAt,

    @Schema(description = "Name of the reviewer.", example = "Jane Doe", defaultValue = "Anonymous")
    String reviewerName,

    @Schema(description = "Total number of comments on the review.", example = "5")
    Integer totalComments // New field to track the number of comments
) {
}