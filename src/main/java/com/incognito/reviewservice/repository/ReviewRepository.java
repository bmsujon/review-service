package com.incognito.reviewservice.repository;

import com.incognito.reviewservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Import this
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> { // Add JpaSpecificationExecutor
    // You can add custom query methods here if needed later
}