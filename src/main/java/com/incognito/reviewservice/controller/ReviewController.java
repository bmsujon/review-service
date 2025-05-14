package com.incognito.reviewservice.controller;

import com.incognito.reviewservice.dto.ReviewCreateRequest;
import com.incognito.reviewservice.dto.ReviewResponse;
import com.incognito.reviewservice.model.ReviewType; // Import ReviewType
import com.incognito.reviewservice.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject; // For Pageable in Swagger
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.data.web.PageableDefault; // For default pagination
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "Review API", description = "APIs for managing reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "Create a new review", description = "Creates a new review based on the provided data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Review created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class)))
    })
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewCreateRequest request) {
        ReviewResponse reviewResponse = reviewService.createReview(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest() // Reverted to fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(reviewResponse.id())
                .toUri();
        return ResponseEntity.created(location).body(reviewResponse);
    }

    @Operation(summary = "Get a review by its ID", description = "Retrieves details of a specific review.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review found successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "404", description = "Review not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getReviewById(
            @Parameter(description = "ID of the review to be retrieved", required = true, example = "1")
            @PathVariable Long id) {
        ReviewResponse reviewResponse = reviewService.getReviewById(id);
        return ResponseEntity.ok(reviewResponse);
    }

    @Operation(summary = "Get a list of reviews", description = "Retrieves a paginated list of reviews with optional filters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of reviews",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Page.class))), // Note: Schema is Page, items will be ReviewResponse
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters or pagination settings",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class)))
    })
    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @Parameter(description = "Filter by company name (case-insensitive partial match)", example = "Incognito")
            @RequestParam(required = false) String companyName,
            @Parameter(description = "Filter by review type", schema = @Schema(implementation = ReviewType.class))
            @RequestParam(required = false) ReviewType reviewType,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) { // @ParameterObject for Pageable
        Page<ReviewResponse> reviewPage = reviewService.getReviews(companyName, reviewType, pageable);
        return ResponseEntity.ok(reviewPage);
    }

    @Operation(summary = "Increment the like count of a review", description = "Increments the like counter for the specified review by one.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Like count incremented successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "404", description = "Review not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class)))
    })
    @PutMapping("/{reviewId}/like")
    public ResponseEntity<ReviewResponse> likeReview(
            @Parameter(description = "ID of the review to be liked", required = true, example = "1")
            @PathVariable Long reviewId) {
        ReviewResponse updatedReview = reviewService.incrementLikeCount(reviewId);
        return ResponseEntity.ok(updatedReview);
    }

    @Operation(summary = "Increment the dislike count of a review", description = "Increments the dislike counter for the specified review by one.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dislike count incremented successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "404", description = "Review not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Object.class)))
    })
    @PutMapping("/{reviewId}/dislike")
    public ResponseEntity<ReviewResponse> dislikeReview(
            @Parameter(description = "ID of the review to be disliked", required = true, example = "1")
            @PathVariable Long reviewId) {
        ReviewResponse updatedReview = reviewService.incrementDislikeCount(reviewId);
        return ResponseEntity.ok(updatedReview);
    }
}