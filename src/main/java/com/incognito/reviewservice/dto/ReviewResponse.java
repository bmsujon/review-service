package com.incognito.reviewservice.dto;

import com.incognito.reviewservice.model.ReviewStatus;
import com.incognito.reviewservice.model.ReviewType;
import io.swagger.v3.oas.annotations.media.Schema; // Import
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "Data Transfer Object representing a review.")
public class ReviewResponse {

    @Schema(description = "Unique identifier of the review.", example = "1")
    private Long id;

    @Schema(description = "Type of the review.", example = "COMPANY_REVIEW")
    private ReviewType reviewType;

    @Schema(description = "Title of the review.", example = "Excellent Work Environment")
    private String title;

    @Schema(description = "Main content/body of the review.", example = "The company offers great benefits and a supportive team.")
    private String content;

    @Schema(description = "IP address of the user who submitted the review.", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "Number of likes the review has received.", example = "10")
    private Integer likeCount;

    @Schema(description = "Number of dislikes the review has received.", example = "1")
    private Integer dislikeCount;

    @Schema(description = "Indicates if the review has any comments.", example = "true")
    private Boolean hasComment;

    @Schema(description = "Current status of the review (e.g., PENDING, APPROVED, REJECTED).", example = "APPROVED")
    private ReviewStatus status;

    @Schema(description = "Indicates if the reviewer was an employee.", example = "true")
    private Boolean isEmployee;

    @Schema(description = "Department of the reviewer, if applicable.", example = "Engineering")
    private String dept;

    @Schema(description = "Role of the reviewer, if applicable.", example = "Software Developer")
    private String role;

    @Schema(description = "Name of the company being reviewed.", example = "Incognito Tech")
    private String companyName;

    @Schema(description = "Website URL related to the review.", example = "https://incognito.example.com")
    private String website;

    @Schema(description = "The date when the work/experience started.", example = "2022-01-15T00:00:00Z")
    private Instant workStartDate;

    @Schema(description = "The date when the work/experience ended.", example = "2023-01-15T00:00:00Z")
    private Instant workEndDate;

    @Schema(description = "Timestamp of when the review was created.", example = "2023-10-27T10:30:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp of when the review was last updated.", example = "2023-10-27T11:00:00Z")
    private Instant updatedAt;
}