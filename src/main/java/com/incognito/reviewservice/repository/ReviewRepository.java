package com.incognito.reviewservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Import this
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.incognito.reviewservice.entity.Review;
import com.incognito.reviewservice.model.ReviewType; // Added for ReviewType

import jakarta.persistence.LockModeType; // Added for List

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> { // Add JpaSpecificationExecutor
    // You can add custom query methods here if needed later
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Review> findById(Long id); // Override to lock

    @Modifying
    @Query("UPDATE Review r SET r.likeCount = r.likeCount + 1 WHERE r.id = :reviewId")
    int incrementLikeCount(@Param("reviewId") Long reviewId);

    @Modifying
    @Query("UPDATE Review r SET r.dislikeCount = r.dislikeCount + 1 WHERE r.id = :reviewId")
    int incrementDislikeCount(@Param("reviewId") Long reviewId);

    // --- Methods for Review Statistics ---

    @Query(value = "SELECT COUNT(r.id) FROM reviews r " +
                   "WHERE (:companyName IS NULL OR LOWER(r.company_name) LIKE LOWER(CONCAT('%', CAST(:companyName AS VARCHAR), '%'))) " +
                   "AND (CAST(:reviewType AS VARCHAR) IS NULL OR r.review_type = CAST(:reviewType AS VARCHAR))", // reviewType parameter consistently cast to VARCHAR
           nativeQuery = true)
    long countFilteredReviews(@Param("companyName") String companyName, @Param("reviewType") ReviewType reviewType);

    @Query(value = "SELECT SUM(r.like_count) FROM reviews r " +
                   "WHERE (:companyName IS NULL OR LOWER(r.company_name) LIKE LOWER(CONCAT('%', CAST(:companyName AS VARCHAR), '%'))) " +
                   "AND (CAST(:reviewType AS VARCHAR) IS NULL OR r.review_type = CAST(:reviewType AS VARCHAR))", // reviewType parameter consistently cast to VARCHAR
           nativeQuery = true)
    Long sumLikesFiltered(@Param("companyName") String companyName, @Param("reviewType") ReviewType reviewType);

    @Query(value = "SELECT SUM(r.dislike_count) FROM reviews r " +
                   "WHERE (:companyName IS NULL OR LOWER(r.company_name) LIKE LOWER(CONCAT('%', CAST(:companyName AS VARCHAR), '%'))) " +
                   "AND (CAST(:reviewType AS VARCHAR) IS NULL OR r.review_type = CAST(:reviewType AS VARCHAR))", // reviewType parameter consistently cast to VARCHAR
           nativeQuery = true)
    Long sumDislikesFiltered(@Param("companyName") String companyName, @Param("reviewType") ReviewType reviewType);

    // This query returns a List of Object arrays, where each array is [String review_type, Long count]
    @Query(value = "SELECT r.review_type, COUNT(r.id) FROM reviews r " +
                   "WHERE (:companyName IS NULL OR LOWER(r.company_name) LIKE LOWER(CONCAT('%', CAST(:companyName AS VARCHAR), '%'))) " +
                   "AND (CAST(:reviewType AS VARCHAR) IS NULL OR r.review_type = CAST(:reviewType AS VARCHAR)) " + // reviewType parameter consistently cast to VARCHAR
                   "GROUP BY r.review_type",
           nativeQuery = true)
    List<Object[]> getReviewCountsByTypeFiltered(@Param("companyName") String companyName, @Param("reviewType") ReviewType reviewType);
}