package com.incognito.reviewservice.service;

import com.incognito.reviewservice.dto.CommentCreateRequest;
import com.incognito.reviewservice.dto.CommentResponse;
import com.incognito.reviewservice.entity.Comment;
import com.incognito.reviewservice.entity.Review;
import com.incognito.reviewservice.exception.BadRequestException;
import com.incognito.reviewservice.exception.ResourceNotFoundException;
import com.incognito.reviewservice.repository.CommentRepository;
import com.incognito.reviewservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public CommentResponse createComment(Long reviewId, Long parentId, CommentCreateRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId + " to add comment."));

        Comment comment = Comment.builder()
                .content(request.content()) // Changed from request.getContent()
                .review(review) // Always associate the comment with the review
                .commenterName(request.commenterName()) // Use name from request, can be null
                .build();

        if (parentId != null) {
            Comment parentComment = commentRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found with id: " + parentId));

            // Validate that the parent comment belongs to the same review
            if (parentComment.getReview() == null || !parentComment.getReview().getId().equals(reviewId)) {
                throw new BadRequestException("Parent comment with id " + parentId + " does not belong to review with id " + reviewId);
            }
            comment.setParent(parentComment);
        }

        // Save the comment entity itself. The returned instance is managed and has the ID.
        Comment savedComment = commentRepository.save(comment);

        return mapToCommentResponse(savedComment);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByReviewId(Long reviewId, Pageable pageable) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Review not found with id: " + reviewId);
        }
        Page<Comment> commentPage = commentRepository.findByReviewIdAndParentIsNull(reviewId, pageable);
        return commentPage.map(this::mapToCommentResponse);
    }

    /**
     * Increments the like count for a given comment.
     *
     * @param reviewId  The ID of the review the comment belongs to (for validation).
     * @param commentId The ID of the comment to like.
     * @return A {@link CommentResponse} representing the updated comment.
     * @throws ResourceNotFoundException if the comment is not found.
     * @throws BadRequestException if the comment does not belong to the specified review.
     */
    @Transactional
    public CommentResponse incrementLikeCount(Long reviewId, Long commentId) {
        int updatedRows = commentRepository.incrementLikeCount(commentId, reviewId);
        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId + " for review id: " + reviewId + " to increment like count.");
        }
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId)); // Should not happen if update was successful
        return mapToCommentResponse(comment);
    }

    /**
     * Increments the dislike count for a given comment.
     *
     * @param reviewId  The ID of the review the comment belongs to (for validation).
     * @param commentId The ID of the comment to dislike.
     * @return A {@link CommentResponse} representing the updated comment.
     * @throws ResourceNotFoundException if the comment is not found or does not belong to the review.
     */
    @Transactional
    public CommentResponse incrementDislikeCount(Long reviewId, Long commentId) {
        int updatedRows = commentRepository.incrementDislikeCount(commentId, reviewId);
        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId + " for review id: " + reviewId + " to increment dislike count.");
        }
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId)); // Should not happen if update was successful
        return mapToCommentResponse(comment);
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        if (comment == null) {
            return null;
        }
        return new CommentResponse( // Changed from CommentResponse.builder()
                comment.getId(),
                comment.getContent(),
                comment.getLikeCount(),
                comment.getDislikeCount(),
                comment.getReview() != null ? comment.getReview().getId() : null,
                comment.getParent() != null ? comment.getParent().getId() : null,
                comment.getCreatedAt(),
                comment.getUpdatedAt(), // Order might need adjustment based on record definition
                comment.getStatus(),
                comment.hasAnyReply(),
                ObjectUtils.isEmpty(comment.getCommenterName()) ? "Anonymous" : comment.getCommenterName(), // Default to Anonymous if null/empty
                comment.getTotalReplies()
        );
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getRepliesOfComment(Long reviewId, Long commentId, Pageable pageable) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Review not found with id: " + reviewId);
        }
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId);
        }
        Page<Comment> commentPage = commentRepository.findByParentId(commentId, pageable);
        return commentPage.map(this::mapToCommentResponse);
    }
}