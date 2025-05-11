package com.incognito.reviewservice.repository;

import com.incognito.reviewservice.entity.Comment;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // You can add custom query methods here if needed later

    /**
     * Finds all comments associated with a given review ID, with pagination.
     *
     * @param reviewId The ID of the review.
     * @param pageable Pagination information.
     * @return A page of comments.
     */
    Page<Comment> findByReviewId(Long reviewId, Pageable pageable);
}