package com.incognito.reviewservice.entity;

import com.incognito.reviewservice.model.ReviewStatus;
import com.incognito.reviewservice.model.ReviewType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank; // Added
import jakarta.validation.constraints.Size;
import lombok.*; // Ensure EqualsAndHashCode and ToString are covered or import individually
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Formula;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false) // Added
@ToString(exclude = {"comments", "contentHtml"}) // Added
@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_reviews_status", columnList = "status"),
        @Index(name = "idx_reviews_review_type", columnList = "review_type"),
        @Index(name = "idx_reviews_company_name", columnList = "company_name"),
        @Index(name = "idx_reviews_created_at", columnList = "created_at")
})
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false)
    private ReviewType reviewType;

    @NotBlank // Potentially added
    @Size(max = 255)
    private String title;

    @NotBlank // Potentially added
    @Lob
    @Column(name = "content_html", columnDefinition = "TEXT")
    private String contentHtml;

    // ... other fields remain the same ...
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Builder.Default
    @Column(name = "like_count", nullable = false)
    @ColumnDefault("0")
    private Integer likeCount = 0;

    @Builder.Default
    @Column(name = "dislike_count", nullable = false)
    @ColumnDefault("0")
    private Integer dislikeCount = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @ColumnDefault("'PENDING'")
    private ReviewStatus status = ReviewStatus.PENDING;

    @Builder.Default
    @Column(name = "is_employee", nullable = false)
    @ColumnDefault("false")
    private Boolean isEmployee = false;

    @Size(max = 100)
    @Column(length = 100)
    private String dept;

    @Size(max = 100)
    @Column(length = 100)
    private String role;

    // @NotBlank // Potentially add if companyName is mandatory
    @Size(max = 255)
    @Column(name = "company_name", length = 255)
    private String companyName;

    @Size(max = 2048)
    @Column(length = 2048)
    private String website;

    @Column(name = "work_start_date")
    private Instant workStartDate;

    @Column(name = "work_end_date")
    private Instant workEndDate;

    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @ColumnDefault("'Anonymous'")
    @Column(name = "reviewer_name", length = 100)
    private String reviewerName = "Anonymous";

    @Formula("(SELECT COUNT(*) FROM comments c WHERE c.review_id = id)")
    private Integer totalComments;

    /**
     * Returns an unmodifiable view of the comments associated with this review.
     * Modifications to the comment list should be done via {@link #addComment(Comment)}
     * and {@link #removeComment(Comment)}.
     *
     * @return An unmodifiable list of comments.
     */
    public List<Comment> getComments() {
        return Collections.unmodifiableList(this.comments);
    }

    /**
     * Checks if this review has any comments.
     * This is derived from {@link #getTotalComments()}.
     *
     * @return true if the review has one or more comments, false otherwise.
     */
    public boolean hasAnyComment() {
        return this.totalComments != null && this.totalComments > 0;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.setReview(this);
        // Note: 'totalComments' (and thus 'hasAnyComment()') will only update
        // in the database and reflect on this entity instance upon the next fetch.
    }

    public void removeComment(Comment comment) {
        boolean removed = this.comments.remove(comment);
        if (removed) {
            comment.setReview(null);
        }
        // Note: 'totalComments' (and thus 'hasAnyComment()') will only update
        // in the database and reflect on this entity instance upon the next fetch.
    }
}