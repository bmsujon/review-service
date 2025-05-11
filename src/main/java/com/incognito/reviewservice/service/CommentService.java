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
                .content(request.getContent())
                // Default values for likeCount, dislikeCount, status will be applied by @Builder.Default
                .build();

        if (parentId != null) {
            Comment parentComment = commentRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found with id: " + parentId));

            if (parentComment.getReview() == null || !parentComment.getReview().getId().equals(reviewId)) {
                throw new BadRequestException("Parent comment with id " + parentId + " does not belong to review with id " + reviewId);
            }
            parentComment.addReply(comment);
            commentRepository.save(parentComment); // Cascades to save the new reply 'comment'
        } else {
            review.addComment(comment);
            reviewRepository.save(review); // Cascades to save the new 'comment'
        }
        return mapToCommentResponse(comment);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByReviewId(Long reviewId, Pageable pageable) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Review not found with id: " + reviewId);
        }
        Page<Comment> commentPage = commentRepository.findByReviewId(reviewId, pageable);
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
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        // Validate that the comment belongs to the specified review
        if (comment.getReview() == null || !comment.getReview().getId().equals(reviewId)) {
            throw new BadRequestException("Comment with id " + commentId + " does not belong to review with id " + reviewId);
        }

        comment.setLikeCount(comment.getLikeCount() + 1);
        // As with reviews, more complex logic would be needed if a user can only like once.
        Comment updatedComment = commentRepository.save(comment);
        return mapToCommentResponse(updatedComment);
    }

    /**
     * Increments the dislike count for a given comment.
     *
     * @param reviewId  The ID of the review the comment belongs to (for validation).
     * @param commentId The ID of the comment to dislike.
     * @return A {@link CommentResponse} representing the updated comment.
     * @throws ResourceNotFoundException if the comment is not found.
     * @throws BadRequestException if the comment does not belong to the specified review.
     */
    @Transactional
    public CommentResponse incrementDislikeCount(Long reviewId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        // Validate that the comment belongs to the specified review
        if (comment.getReview() == null || !comment.getReview().getId().equals(reviewId)) {
            throw new BadRequestException("Comment with id " + commentId + " does not belong to review with id " + reviewId);
        }

        comment.setDislikeCount(comment.getDislikeCount() + 1);
        // Similar to likes, more complex logic would be needed if a user can only dislike once.
        Comment updatedComment = commentRepository.save(comment);
        return mapToCommentResponse(updatedComment);
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        if (comment == null) {
            return null;
        }
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .dislikeCount(comment.getDislikeCount())
                // .status(comment.getStatus()) // If you have status
                .reviewId(comment.getReview() != null ? comment.getReview().getId() : null)
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}