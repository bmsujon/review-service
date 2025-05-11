package com.incognito.reviewservice.entity;

import com.incognito.reviewservice.model.ReviewStatus;
import com.incognito.reviewservice.model.ReviewType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_reviews_status", columnList = "status"),
    @Index(name = "idx_reviews_review_type", columnList = "review_type"),
    @Index(name = "idx_reviews_company_name", columnList = "company_name"),
    @Index(name = "idx_reviews_created_at", columnList = "created_at")
    // Add index for company_id if you use the Company entity and link it
    // @Index(name = "idx_reviews_company_id", columnList = "company_id")
})
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false)
    private ReviewType reviewType;

    @Size(max = 255)
    private String title;

    @Lob // Good practice for potentially very long text, though String usually maps to TEXT in Postgres
    @Column(name = "content_html", columnDefinition = "TEXT") // Explicitly use TEXT type
    private String contentHtml;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Builder.Default
    @Column(name = "like_count", nullable = false) // Let Hibernate determine the integer type
    @ColumnDefault("0") // Hibernate annotation for DB default
    private Integer likeCount = 0; // Java object default

    @Builder.Default
    @Column(name = "dislike_count", nullable = false)
    @ColumnDefault("0")
    private Integer dislikeCount = 0;

    // After:
    @Builder.Default
    @Column(name = "has_comment", nullable = false) // Let Hibernate determine the boolean type
    @ColumnDefault("false") // Hibernate annotation for DB default
    private Boolean hasComment = false; // Java object default


    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50) // Specify VARCHAR length separately
    @ColumnDefault("'PENDING'") // DB default for the string enum, note the single quotes
    private ReviewStatus status = ReviewStatus.PENDING; // Java object default

    @Builder.Default
    @Column(name = "is_employee", nullable = false)
    @ColumnDefault("false")
    private Boolean isEmployee = false;

    @Column(length = 100)
    private String dept;

    @Column(length = 100)
    private String role;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(length = 2048)
    private String website;

    @Column(name = "work_start_date")
    private Instant workStartDate;

    @Column(name = "work_end_date")
    private Instant workEndDate;

    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    // Helper methods for bidirectional relationship (optional but good practice)
    // In Review.java
    // ... other fields and annotations ...
    // import com.incognito.reviewservice.entity.Comment; // Ensure this import
    // import java.util.List;
    // import java.util.ArrayList;

    public void addComment(Comment comment) {
        if (this.comments == null) {
            this.comments = new ArrayList<>();
        }
        this.comments.add(comment);
        comment.setReview(this); // This line is crucial for direct comments
        this.setHasComment(true);
    }


    public void removeComment(Comment comment) {
        if (this.comments != null) {
            this.comments.remove(comment);
        }
        comment.setReview(null);
    }
}