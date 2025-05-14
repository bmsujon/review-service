package com.incognito.reviewservice.entity;

import com.incognito.reviewservice.model.CommentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_comments_review_id", columnList = "review_id"), // review_id is the FK column
    @Index(name = "idx_comments_parent_id", columnList = "parent_id"), // parent_id is the FK column
    @Index(name = "idx_comments_created_at", columnList = "created_at")
})
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id") // This column in 'comments' table references another 'comments.id'
    private Comment parent;

    @Builder.Default
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> replies = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false) // This column in 'comments' table references 'reviews.id'
    private Review review;

    @Column(name = "user_name", length = 100)
    private String userName; // Or this could be a UUID referencing a User entity

    @NotBlank
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Builder.Default
    @Column(name = "like_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer likeCount = 0;

    @Builder.Default
    @Column(name = "dislike_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer dislikeCount = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50) // Specify VARCHAR length
    @ColumnDefault("'ACTIVE'") // DB default for the string enum, note single quotes
    private CommentStatus status = CommentStatus.ACTIVE; // Java object default

    @Formula("(EXISTS (SELECT 1 FROM comments r WHERE r.parent_id = id))")
    private boolean hasReplies;

    @Builder.Default
    @Column(name = "commenter_name", length = 100)
    @ColumnDefault("'Anonymous'") // Default value for the column, with single quotes
    private String commenterName = "Anonymous"; // Default value for anonymous comments
    // this field will not be int DB, but will be used in the application logic

    @Formula("(SELECT COUNT(*) FROM comments r WHERE r.parent_id = id)")
    // This field is not stored in the database but is calculated on the fly
    private Integer totalReplies;
    // Helper methods for bidirectional relationship (optional)
    public void addReply(Comment reply) {
        if (this.replies == null) {
            this.replies = new ArrayList<>();
        }
        this.replies.add(reply);
        reply.setParent(this);
        reply.setReview(this.getReview()); // This line is crucial for replies
    }


    public void removeReply(Comment reply) {
        if (this.replies != null) {
            this.replies.remove(reply);
        }
        reply.setParent(null);
    }
}