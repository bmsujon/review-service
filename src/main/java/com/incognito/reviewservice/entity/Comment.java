package com.incognito.reviewservice.entity;

import com.incognito.reviewservice.model.CommentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id", callSuper = false) // Added
@ToString(exclude = {"parent", "replies", "review", "content"}) // Added
@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comments_review_id", columnList = "review_id"),
        @Index(name = "idx_comments_parent_id", columnList = "parent_id"),
        @Index(name = "idx_comments_created_at", columnList = "created_at")
})
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Builder.Default
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> replies = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    // Consider clarifying the purpose of userName vs. commenterName.
    // If userName is a user-provided string, add @Size.
    @Column(name = "user_name", length = 100)
    // @Size(max = 100) // Example if it's a user-provided name
    private String userName;

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
    @Column(nullable = false, length = 50)
    @ColumnDefault("'ACTIVE'")
    private CommentStatus status = CommentStatus.ACTIVE;

    @Builder.Default
    @ColumnDefault("'Anonymous'")
    @Column(name = "commenter_name", length = 100)
    // @Size(max = 100) // Example
    private String commenterName = "Anonymous";

    @Formula("(SELECT COUNT(*) FROM comments r WHERE r.parent_id = id)")
    private Integer totalReplies;

    /**
     * Checks if this comment has any replies.
     * This is derived from {@link #getTotalReplies()}.
     *
     * @return true if the comment has one or more replies, false otherwise.
     */
    public boolean hasAnyReply() {
        return this.totalReplies != null && this.totalReplies > 0;
    }

    /**
     * Returns an unmodifiable view of the replies to this comment.
     * Modifications to the reply list should be done via {@link #addReply(Comment)}
     * and {@link #removeReply(Comment)}.
     *
     * @return An unmodifiable list of replies.
     */
    public List<Comment> getReplies() {
        return Collections.unmodifiableList(this.replies);
    }

    /**
     * Adds a reply to this comment and sets up the bidirectional relationship.
     * Also ensures the reply is associated with the same review as this comment.
     *
     * @param reply The comment to add as a reply.
     */
    public void addReply(Comment reply) {
        this.replies.add(reply);
        reply.setParent(this);
        if (this.getReview() != null) {
            reply.setReview(this.getReview());
        }
        // Note: 'totalReplies' (and thus 'hasReplies()') will only update
        // in the database and reflect on this entity instance upon the next fetch.
    }

    /**
     * Removes a reply from this comment and clears the bidirectional relationship.
     *
     * @param reply The comment to remove.
     */
    public void removeReply(Comment reply) {
        boolean removed = this.replies.remove(reply);
        if (removed) {
            reply.setParent(null);
        }
        // Note: 'totalReplies' (and thus 'hasReplies()') will only update
        // in the database and reflect on this entity instance upon the next fetch.
    }
}
