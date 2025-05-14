package com.incognito.reviewservice.service;

import com.incognito.reviewservice.dto.CommentResponse;
import com.incognito.reviewservice.dto.CommentCreateRequest;
import com.incognito.reviewservice.entity.Comment;
import com.incognito.reviewservice.entity.Review;
import com.incognito.reviewservice.repository.CommentRepository;
import com.incognito.reviewservice.repository.ReviewRepository;
import com.incognito.reviewservice.exception.BadRequestException;
import com.incognito.reviewservice.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTests {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private CommentService commentService;

    private Review review;
    private Comment comment;
    private Instant fixedTime;

    @BeforeEach
    void setUp() {
        fixedTime = Instant.now();
        review = new Review();
        review.setId(1L);

        comment = Comment.builder()
                .id(1L)
                .content("Test comment")
                .review(review)
                .likeCount(0)
                .dislikeCount(0)
                .commenterName("Original Commenter")
                .status(com.incognito.reviewservice.model.CommentStatus.ACTIVE)
                .hasReplies(false)
                .totalReplies(0)
                .parent(null)
                .build();
        // Set BaseEntity fields after building
        comment.setCreatedAt(fixedTime.minusSeconds(3600));
        comment.setUpdatedAt(fixedTime.minusSeconds(1800));
        comment.setVersion(1); // Assuming a default version
    }

    @Test
    void incrementLikeCount_Success() {
        Comment likedComment = Comment.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .review(comment.getReview())
                .likeCount(comment.getLikeCount() + 1)
                .dislikeCount(comment.getDislikeCount())
                .commenterName(comment.getCommenterName())
                .status(comment.getStatus())
                .hasReplies(comment.isHasReplies())
                .totalReplies(comment.getTotalReplies())
                .parent(comment.getParent())
                .build();
        // Set BaseEntity fields after building
        likedComment.setCreatedAt(comment.getCreatedAt()); // Should retain original creation time
        likedComment.setUpdatedAt(fixedTime); // Should be updated
        likedComment.setVersion(comment.getVersion() == null ? 1 : comment.getVersion() + 1);

        when(commentRepository.incrementLikeCount(comment.getId(), review.getId())).thenReturn(1);
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(likedComment));

        CommentResponse response = commentService.incrementLikeCount(review.getId(), comment.getId());

        assertNotNull(response);
        assertEquals(likedComment.getId(), response.id());
        assertEquals(likedComment.getContent(), response.content());
        assertEquals(likedComment.getLikeCount(), response.likeCount());
        assertEquals(likedComment.getDislikeCount(), response.dislikeCount());
        assertEquals(likedComment.getReview().getId(), response.reviewId());
        assertNull(response.parentId());
        assertEquals(likedComment.getCreatedAt(), response.createdAt());
        assertEquals(likedComment.getUpdatedAt(), response.updatedAt());
        assertEquals(likedComment.getStatus(), response.status());
        assertEquals(likedComment.isHasReplies(), response.hasReplies());
        assertEquals(likedComment.getCommenterName(), response.commenterName());
        assertEquals(likedComment.getTotalReplies(), response.totalReplies());

        verify(commentRepository, times(1)).incrementLikeCount(comment.getId(), review.getId());
        verify(commentRepository, times(1)).findById(comment.getId());
    }

    @Test
    void incrementDislikeCount_Success() {
        Comment dislikedComment = Comment.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .review(review)
                .likeCount(comment.getLikeCount())
                .dislikeCount(comment.getDislikeCount() + 1)
                .commenterName(comment.getCommenterName())
                .status(comment.getStatus())
                .hasReplies(comment.isHasReplies())
                .totalReplies(comment.getTotalReplies())
                .parent(comment.getParent())
                .build();
        // Set BaseEntity fields after building
        dislikedComment.setCreatedAt(comment.getCreatedAt()); // Should retain original creation time
        dislikedComment.setUpdatedAt(fixedTime); // Should be updated
        dislikedComment.setVersion(comment.getVersion() == null ? 1 : comment.getVersion() + 1);

        when(commentRepository.incrementDislikeCount(comment.getId(), review.getId())).thenReturn(1);
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(dislikedComment));

        CommentResponse response = commentService.incrementDislikeCount(review.getId(), comment.getId());

        assertNotNull(response);
        assertEquals(dislikedComment.getId(), response.id());
        assertEquals(dislikedComment.getContent(), response.content());
        assertEquals(dislikedComment.getLikeCount(), response.likeCount());
        assertEquals(dislikedComment.getDislikeCount(), response.dislikeCount());
        assertEquals(dislikedComment.getReview().getId(), response.reviewId());
        assertNull(response.parentId());
        assertEquals(dislikedComment.getCreatedAt(), response.createdAt());
        assertEquals(dislikedComment.getUpdatedAt(), response.updatedAt());
        assertEquals(dislikedComment.getStatus(), response.status());
        assertEquals(dislikedComment.isHasReplies(), response.hasReplies());
        assertEquals(dislikedComment.getCommenterName(), response.commenterName());
        assertEquals(dislikedComment.getTotalReplies(), response.totalReplies());

        verify(commentRepository, times(1)).incrementDislikeCount(comment.getId(), review.getId());
        verify(commentRepository, times(1)).findById(comment.getId());
    }

    @Test
    void createComment_whenParentIdIsNull_shouldCreateTopLevelComment_withProvidedName() {
        String commenterNameFromRequest = "Test Commenter";
        CommentCreateRequest request = new CommentCreateRequest("Test content", commenterNameFromRequest);

        Comment commentReturnedBySave = Comment.builder()
                .id(2L)
                .content(request.content())
                .review(review)
                .commenterName(request.commenterName())
                .status(com.incognito.reviewservice.model.CommentStatus.ACTIVE)
                .likeCount(0)
                .dislikeCount(0)
                .hasReplies(false)
                .totalReplies(0)
                .parent(null)
                .build();
        // Set BaseEntity fields after building
        commentReturnedBySave.setCreatedAt(fixedTime);
        commentReturnedBySave.setUpdatedAt(fixedTime);
        commentReturnedBySave.setVersion(1);

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(commentRepository.save(any(Comment.class))).thenReturn(commentReturnedBySave);

        CommentResponse response = commentService.createComment(review.getId(), null, request);

        assertNotNull(response);
        assertEquals(commentReturnedBySave.getId(), response.id());
        assertEquals(request.content(), response.content());
        assertEquals(commenterNameFromRequest, response.commenterName());
        assertNull(response.parentId());
        assertEquals(0, response.likeCount());
        assertEquals(0, response.dislikeCount());
        assertEquals(com.incognito.reviewservice.model.CommentStatus.ACTIVE, response.status());
        assertFalse(response.hasReplies());
        assertEquals(0, response.totalReplies());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());
        assertEquals(review.getId(), response.reviewId());

        verify(reviewRepository, times(1)).findById(review.getId());
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository, times(1)).save(commentCaptor.capture());
        Comment capturedComment = commentCaptor.getValue();
        assertEquals(request.content(), capturedComment.getContent());
        assertEquals(request.commenterName(), capturedComment.getCommenterName());
        assertEquals(review, capturedComment.getReview());
        assertNull(capturedComment.getParent());
    }

    @Test
    void createComment_whenParentIdIsNull_shouldCreateTopLevelComment_withDefaultName() {
        CommentCreateRequest request = new CommentCreateRequest("Test content", null);

        Comment commentReturnedBySave = Comment.builder()
                .id(3L)
                .content(request.content())
                .review(review)
                .commenterName("Anonymous")
                .status(com.incognito.reviewservice.model.CommentStatus.ACTIVE)
                .likeCount(0)
                .dislikeCount(0)
                .hasReplies(false)
                .totalReplies(0)
                .parent(null)
                .build();
        // Set BaseEntity fields after building
        commentReturnedBySave.setCreatedAt(fixedTime);
        commentReturnedBySave.setUpdatedAt(fixedTime);
        commentReturnedBySave.setVersion(1);

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(commentRepository.save(any(Comment.class))).thenReturn(commentReturnedBySave);

        CommentResponse response = commentService.createComment(review.getId(), null, request);

        assertNotNull(response);
        assertEquals(commentReturnedBySave.getId(), response.id());
        assertEquals(request.content(), response.content());
        assertEquals("Anonymous", response.commenterName());
        assertNull(response.parentId());
        assertEquals(0, response.likeCount());
        assertEquals(0, response.dislikeCount());
        assertEquals(com.incognito.reviewservice.model.CommentStatus.ACTIVE, response.status());
        assertFalse(response.hasReplies());
        assertEquals(0, response.totalReplies());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());
        assertEquals(review.getId(), response.reviewId());

        verify(reviewRepository, times(1)).findById(review.getId());
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository, times(1)).save(commentCaptor.capture());
        Comment capturedComment = commentCaptor.getValue();
        assertEquals(request.content(), capturedComment.getContent());
        assertNull(capturedComment.getCommenterName());
        assertEquals(review, capturedComment.getReview());
        assertNull(capturedComment.getParent());
    }

    @Test
    void createComment_whenParentIdIsNotNull_shouldCreateReplyComment() {
        String replyContent = "Reply content";
        String replierName = "Reply Guy";
        CommentCreateRequest request = new CommentCreateRequest(replyContent, replierName);

        Comment parentComment = Comment.builder()
                .id(2L)
                .content("Parent comment")
                .review(review)
                .commenterName("Parent Commenter")
                .status(com.incognito.reviewservice.model.CommentStatus.ACTIVE)
                .likeCount(5)
                .dislikeCount(1)
                .hasReplies(true)
                .totalReplies(1)
                .build();
        // Set BaseEntity fields after building
        parentComment.setCreatedAt(fixedTime.minusSeconds(1000));
        parentComment.setUpdatedAt(fixedTime.minusSeconds(500));
        parentComment.setVersion(1);

        Comment replyCommentSaved = Comment.builder()
                .id(3L)
                .content(replyContent)
                .review(review)
                .parent(parentComment)
                .commenterName(replierName)
                .status(com.incognito.reviewservice.model.CommentStatus.ACTIVE)
                .likeCount(0)
                .dislikeCount(0)
                .hasReplies(false)
                .totalReplies(0)
                .build();
        // Set BaseEntity fields after building
        replyCommentSaved.setCreatedAt(fixedTime);
        replyCommentSaved.setUpdatedAt(fixedTime);
        replyCommentSaved.setVersion(1);

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(commentRepository.findById(parentComment.getId())).thenReturn(Optional.of(parentComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(replyCommentSaved);

        CommentResponse response = commentService.createComment(review.getId(), parentComment.getId(), request);

        assertNotNull(response);
        assertEquals(replyCommentSaved.getId(), response.id());
        assertEquals(replyContent, response.content());
        assertEquals(replierName, response.commenterName());
        assertEquals(parentComment.getId(), response.parentId());
        assertEquals(review.getId(), response.reviewId());
        assertEquals(0, response.likeCount());
        assertEquals(0, response.dislikeCount());
        assertEquals(com.incognito.reviewservice.model.CommentStatus.ACTIVE, response.status());
        assertFalse(response.hasReplies());
        assertEquals(0, response.totalReplies());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());

        verify(reviewRepository, times(1)).findById(review.getId());
        verify(commentRepository, times(1)).findById(parentComment.getId());
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository, times(1)).save(commentCaptor.capture());
        Comment capturedReply = commentCaptor.getValue();
        assertEquals(replyContent, capturedReply.getContent());
        assertEquals(replierName, capturedReply.getCommenterName());
        assertEquals(review, capturedReply.getReview());
        assertEquals(parentComment, capturedReply.getParent());
    }

    @Test
    void getCommentsByReviewId_shouldReturnPageOfCommentResponses() {
        Long reviewId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        Comment comment1 = Comment.builder().id(1L).content("Comment 1").review(review)
                .commenterName("User1").status(com.incognito.reviewservice.model.CommentStatus.ACTIVE)
                .likeCount(2).dislikeCount(0).hasReplies(true).totalReplies(1)
                .build();
        comment1.setCreatedAt(fixedTime.minusSeconds(200));
        comment1.setUpdatedAt(fixedTime.minusSeconds(100));
        comment1.setVersion(1);

        Comment comment2 = Comment.builder().id(2L).content("Comment 2").review(review)
                .commenterName("User2").status(com.incognito.reviewservice.model.CommentStatus.ACTIVE)
                .likeCount(5).dislikeCount(1).hasReplies(false).totalReplies(0)
                .build();
        comment2.setCreatedAt(fixedTime.minusSeconds(150));
        comment2.setUpdatedAt(fixedTime.minusSeconds(50));
        comment2.setVersion(1);

        List<Comment> comments = Arrays.asList(comment1, comment2);
        Page<Comment> commentPage = new PageImpl<>(comments, pageable, comments.size());

        when(reviewRepository.existsById(reviewId)).thenReturn(true);
        when(commentRepository.findByReviewIdAndParentIsNull(reviewId, pageable)).thenReturn(commentPage);

        Page<CommentResponse> resultPage = commentService.getCommentsByReviewId(reviewId, pageable);

        assertNotNull(resultPage);
        assertEquals(2, resultPage.getTotalElements());
        assertEquals(2, resultPage.getContent().size());

        CommentResponse response1 = resultPage.getContent().get(0);
        assertEquals(comment1.getId(), response1.id());
        assertEquals(comment1.getContent(), response1.content());
        assertEquals(comment1.getCommenterName(), response1.commenterName());
        assertEquals(comment1.getLikeCount(), response1.likeCount());
        assertEquals(comment1.getDislikeCount(), response1.dislikeCount());
        assertEquals(comment1.getStatus(), response1.status());
        assertEquals(comment1.isHasReplies(), response1.hasReplies());
        assertEquals(comment1.getTotalReplies(), response1.totalReplies());
        assertEquals(comment1.getCreatedAt(), response1.createdAt());
        assertEquals(comment1.getUpdatedAt(), response1.updatedAt());
        assertEquals(reviewId, response1.reviewId());
        assertNull(response1.parentId());

        CommentResponse response2 = resultPage.getContent().get(1);
        assertEquals(comment2.getId(), response2.id());
        assertEquals(comment2.getContent(), response2.content());
        assertEquals(comment2.getCommenterName(), response2.commenterName());
        assertEquals(comment2.getLikeCount(), response2.likeCount());
        assertEquals(comment2.getDislikeCount(), response2.dislikeCount());
        assertEquals(comment2.getStatus(), response2.status());
        assertEquals(comment2.isHasReplies(), response2.hasReplies());
        assertEquals(comment2.getTotalReplies(), response2.totalReplies());
        assertEquals(comment2.getCreatedAt(), response2.createdAt());
        assertEquals(comment2.getUpdatedAt(), response2.updatedAt());
        assertEquals(reviewId, response2.reviewId());
        assertNull(response2.parentId());

        verify(reviewRepository, times(1)).existsById(reviewId);
        verify(commentRepository, times(1)).findByReviewIdAndParentIsNull(reviewId, pageable);
    }

    @Test
    void getRepliesOfComment_shouldReturnPageOfCommentResponses() {
        Long reviewId = 1L;
        Long parentCommentId = comment.getId();

        Comment reply1 = Comment.builder().id(2L).content("Reply 1").review(review).parent(comment)
                .commenterName("ReplyUser1").status(com.incognito.reviewservice.model.CommentStatus.ACTIVE)
                .likeCount(1).dislikeCount(0).hasReplies(false).totalReplies(0)
                .build();
        reply1.setCreatedAt(fixedTime.minusSeconds(50));
        reply1.setUpdatedAt(fixedTime.minusSeconds(20));
        reply1.setVersion(1);

        Comment reply2 = Comment.builder().id(3L).content("Reply 2").review(review).parent(comment)
                .commenterName("ReplyUser2").status(com.incognito.reviewservice.model.CommentStatus.ACTIVE)
                .likeCount(0).dislikeCount(0).hasReplies(false).totalReplies(0)
                .build();
        reply2.setCreatedAt(fixedTime.minusSeconds(30));
        reply2.setUpdatedAt(fixedTime.minusSeconds(10));
        reply2.setVersion(1);

        List<Comment> replies = Arrays.asList(reply1, reply2);
        Pageable pageable = PageRequest.of(0, 5);
        Page<Comment> replyPage = new PageImpl<>(replies, pageable, replies.size());

        when(reviewRepository.existsById(reviewId)).thenReturn(true);
        when(commentRepository.existsById(parentCommentId)).thenReturn(true);
        when(commentRepository.findByParentId(parentCommentId, pageable)).thenReturn(replyPage);

        Page<CommentResponse> resultPage = commentService.getRepliesOfComment(reviewId, parentCommentId, pageable);

        assertNotNull(resultPage);
        assertEquals(2, resultPage.getTotalElements());
        assertEquals(2, resultPage.getContent().size());

        CommentResponse response1 = resultPage.getContent().get(0);
        assertEquals(reply1.getId(), response1.id());
        assertEquals(reply1.getContent(), response1.content());
        assertEquals(reply1.getCommenterName(), response1.commenterName());
        assertEquals(reply1.getLikeCount(), response1.likeCount());
        assertEquals(reply1.getDislikeCount(), response1.dislikeCount());
        assertEquals(reply1.getStatus(), response1.status());
        assertEquals(reply1.isHasReplies(), response1.hasReplies());
        assertEquals(reply1.getTotalReplies(), response1.totalReplies());
        assertEquals(reply1.getCreatedAt(), response1.createdAt());
        assertEquals(reply1.getUpdatedAt(), response1.updatedAt());
        assertEquals(reviewId, response1.reviewId());
        assertEquals(parentCommentId, response1.parentId());

        CommentResponse response2 = resultPage.getContent().get(1);
        assertEquals(reply2.getId(), response2.id());
        assertEquals(reply2.getContent(), response2.content());
        assertEquals(reply2.getCommenterName(), response2.commenterName());
        assertEquals(reply2.getLikeCount(), response2.likeCount());
        assertEquals(reply2.getDislikeCount(), response2.dislikeCount());
        assertEquals(reply2.getStatus(), response2.status());
        assertEquals(reply2.isHasReplies(), response2.hasReplies());
        assertEquals(reply2.getTotalReplies(), response2.totalReplies());
        assertEquals(reply2.getCreatedAt(), response2.createdAt());
        assertEquals(reply2.getUpdatedAt(), response2.updatedAt());
        assertEquals(reviewId, response2.reviewId());
        assertEquals(parentCommentId, response2.parentId());

        verify(reviewRepository, times(1)).existsById(reviewId);
        verify(commentRepository, times(1)).existsById(parentCommentId);
        verify(commentRepository, times(1)).findByParentId(parentCommentId, pageable);
    }

    @Test
    void createComment_shouldSetHasRepliesToFalseInitially() {
        String commenterNameFromRequest = "Newbie Poster";
        CommentCreateRequest request = new CommentCreateRequest("New comment content", commenterNameFromRequest);

        Comment savedComment = Comment.builder()
                .id(10L)
                .content(request.content())
                .review(review)
                .commenterName(commenterNameFromRequest)
                .likeCount(0)
                .dislikeCount(0)
                .status(com.incognito.reviewservice.model.CommentStatus.ACTIVE)
                .hasReplies(false)
                .totalReplies(0)
                .parent(null)
                .build();
        // Set BaseEntity fields after building
        savedComment.setCreatedAt(fixedTime);
        savedComment.setUpdatedAt(fixedTime);
        savedComment.setVersion(1);

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentResponse response = commentService.createComment(review.getId(), null, request);

        assertNotNull(response);
        assertEquals(savedComment.getId(), response.id());
        assertEquals(request.content(), response.content());
        assertEquals(commenterNameFromRequest, response.commenterName());
        assertNull(response.parentId());
        assertEquals(0, response.likeCount());
        assertEquals(0, response.dislikeCount());
        assertEquals(com.incognito.reviewservice.model.CommentStatus.ACTIVE, response.status());
        assertFalse(response.hasReplies());
        assertEquals(0, response.totalReplies());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());
        assertEquals(review.getId(), response.reviewId());
    }

    @Test
    void createComment_whenReviewNotFound_shouldThrowResourceNotFoundException() {
        Long reviewId = 99L; // Non-existent review
        CommentCreateRequest request = new CommentCreateRequest("Test content", "Test Commenter");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.createComment(reviewId, null, request);
        });

        verify(reviewRepository, times(1)).findById(reviewId);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void createComment_whenParentCommentNotFound_shouldThrowResourceNotFoundException() {
        Long parentId = 99L; // Non-existent parent comment
        CommentCreateRequest request = new CommentCreateRequest("Test reply", "Test Replier");

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(commentRepository.findById(parentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.createComment(review.getId(), parentId, request);
        });

        verify(reviewRepository, times(1)).findById(review.getId());
        verify(commentRepository, times(1)).findById(parentId);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void createComment_whenParentCommentBelongsToDifferentReview_shouldThrowBadRequestException() {
        Long parentId = 2L;
        CommentCreateRequest request = new CommentCreateRequest("Test reply", "Test Replier");

        Review anotherReview = new Review();
        anotherReview.setId(2L); // Different review ID

        Comment parentCommentFromDifferentReview = Comment.builder()
                .id(parentId)
                .review(anotherReview) // Associated with anotherReview
                .content("Parent from another review")
                .build();

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(commentRepository.findById(parentId)).thenReturn(Optional.of(parentCommentFromDifferentReview));

        assertThrows(BadRequestException.class, () -> {
            commentService.createComment(review.getId(), parentId, request);
        });

        verify(reviewRepository, times(1)).findById(review.getId());
        verify(commentRepository, times(1)).findById(parentId);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void getCommentsByReviewId_whenReviewNotFound_shouldThrowResourceNotFoundException() {
        Long reviewId = 99L; // Non-existent review
        Pageable pageable = PageRequest.of(0, 10);

        when(reviewRepository.existsById(reviewId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.getCommentsByReviewId(reviewId, pageable);
        });

        verify(reviewRepository, times(1)).existsById(reviewId);
        verify(commentRepository, never()).findByReviewIdAndParentIsNull(anyLong(), any(Pageable.class));
    }

    @Test
    void incrementLikeCount_whenCommentNotFoundOrUpdateFails_shouldThrowResourceNotFoundException() {
        Long commentId = comment.getId();
        Long reviewId = review.getId();

        when(commentRepository.incrementLikeCount(commentId, reviewId)).thenReturn(0); // Simulate 0 rows updated

        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.incrementLikeCount(reviewId, commentId);
        });

        verify(commentRepository, times(1)).incrementLikeCount(commentId, reviewId);
        verify(commentRepository, never()).findById(commentId); // Should not be called if update fails
    }

    @Test
    void incrementDislikeCount_whenCommentNotFoundOrUpdateFails_shouldThrowResourceNotFoundException() {
        Long commentId = comment.getId();
        Long reviewId = review.getId();

        when(commentRepository.incrementDislikeCount(commentId, reviewId)).thenReturn(0); // Simulate 0 rows updated

        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.incrementDislikeCount(reviewId, commentId);
        });

        verify(commentRepository, times(1)).incrementDislikeCount(commentId, reviewId);
        verify(commentRepository, never()).findById(commentId); // Should not be called if update fails
    }

    @Test
    void getRepliesOfComment_whenReviewNotFound_shouldThrowResourceNotFoundException() {
        Long reviewId = 99L; // Non-existent review
        Long parentCommentId = comment.getId();
        Pageable pageable = PageRequest.of(0, 5);

        when(reviewRepository.existsById(reviewId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.getRepliesOfComment(reviewId, parentCommentId, pageable);
        });

        verify(reviewRepository, times(1)).existsById(reviewId);
        verify(commentRepository, never()).existsById(anyLong());
        verify(commentRepository, never()).findByParentId(anyLong(), any(Pageable.class));
    }

    @Test
    void getRepliesOfComment_whenParentCommentNotFound_shouldThrowResourceNotFoundException() {
        Long reviewId = review.getId();
        Long parentCommentId = 99L; // Non-existent parent comment
        Pageable pageable = PageRequest.of(0, 5);

        when(reviewRepository.existsById(reviewId)).thenReturn(true);
        when(commentRepository.existsById(parentCommentId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.getRepliesOfComment(reviewId, parentCommentId, pageable);
        });

        verify(reviewRepository, times(1)).existsById(reviewId);
        verify(commentRepository, times(1)).existsById(parentCommentId);
        verify(commentRepository, never()).findByParentId(anyLong(), any(Pageable.class));
    }
}
