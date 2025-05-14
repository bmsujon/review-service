package com.incognito.reviewservice.dto;

import com.incognito.reviewservice.model.ReviewType;
import io.swagger.v3.oas.annotations.media.Schema; // Import
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Schema(description = "Data Transfer Object for creating a new review.")
public record ReviewCreateRequest(
    @NotNull(message = "Review type cannot be null")
    @Schema(description = "Type of the review", requiredMode = Schema.RequiredMode.REQUIRED)
    ReviewType reviewType,

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    @Schema(description = "Title of the review.", minLength = 3, maxLength = 255, example = "Excellent Work Environment")
    String title,

    @NotBlank(message = "Content cannot be blank")
    @Size(min = 10, message = "Content must be at least 10 characters long")
    @Schema(description = "Main content/body of the review.", minLength = 10, example = "The company offers great benefits and a supportive team.")
    String content,

    @Size(max = 45, message = "IP address cannot exceed 45 characters")
    @Schema(description = "IP address of the user submitting the review (optional).", maxLength = 45, example = "192.168.1.100")
    String ipAddress,

    @Size(max = 100, message = "Department cannot exceed 100 characters")
    @Schema(description = "Department of the reviewer, if applicable (optional).", maxLength = 100, example = "Engineering")
    String dept,

    @Size(max = 100, message = "Role cannot exceed 100 characters")
    @Schema(description = "Role of the reviewer, if applicable (optional).", maxLength = 100, example = "Software Developer")
    String role,

    @Size(max = 255, message = "Company name cannot exceed 255 characters")
    @Schema(description = "Name of the company being reviewed (optional).", maxLength = 255, example = "Incognito Tech")
    String companyName,

    @Size(max = 2048, message = "Website URL cannot exceed 2048 characters")
    @Schema(description = "Website URL related to the review (optional).", maxLength = 2048, example = "https://incognito.example.com")
    String website,

    @Schema(description = "Indicates if the reviewer is currently an employee (optional). Defaults to false if not specified.", example = "true")
    Boolean isEmployee,

    @PastOrPresent(message = "Work start date must be in the past or present")
    @Schema(description = "The date when the work/experience started (optional). Must be in the past or present.", example = "2022-01-15T00:00:00Z")
    Instant workStartDate,

    @PastOrPresent(message = "Work end date must be in the past or present")
    @Schema(description = "The date when the work/experience ended (optional). Must be in the past or present.", example = "2023-01-15T00:00:00Z")
    Instant workEndDate,

    @Schema(description = "Name of the reviewer (optional).", example = "Jane Doe", defaultValue = "Anonymous")
    String reviewerName // Optional field for the name of the reviewer
) {
}