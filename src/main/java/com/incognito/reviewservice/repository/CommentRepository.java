package com.incognito.reviewservice.repository;

import com.incognito.reviewservice.entity.Comment;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    Page<Comment> findByReviewIdAndParentIsNull(Long reviewId, Pageable pageable);

    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId AND c.review.id = :reviewId")
    int incrementLikeCount(@Param("commentId") Long commentId, @Param("reviewId") Long reviewId);

    @Modifying
    @Query("UPDATE Comment c SET c.dislikeCount = c.dislikeCount + 1 WHERE c.id = :commentId AND c.review.id = :reviewId")
    int incrementDislikeCount(@Param("commentId") Long commentId, @Param("reviewId") Long reviewId);

    Page<Comment> findByParentId(Long commentId, Pageable pageable);
}