package com.incognito.reviewservice.repository;

import com.incognito.reviewservice.entity.Review;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Import this
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

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
}