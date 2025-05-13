package com.incognito.reviewservice.service;

import com.incognito.reviewservice.dto.CommentResponse;
import com.incognito.reviewservice.dto.CommentCreateRequest;
import com.incognito.reviewservice.entity.Comment;
import com.incognito.reviewservice.entity.Review;
import com.incognito.reviewservice.exception.ResourceNotFoundException;
import com.incognito.reviewservice.repository.CommentRepository;
import com.incognito.reviewservice.repository.ReviewRepository;
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

    @BeforeEach
    void setUp() {
        review = new Review();
        review.setId(1L);

        comment = Comment.builder()
                .id(1L)
                .content("Test comment")
                .review(review)
                .likeCount(0)
                .dislikeCount(0)
                .build();
    }

    @Test
    void incrementLikeCount_Success() {
        when(commentRepository.incrementLikeCount(comment.getId(), review.getId())).thenReturn(1);
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        CommentResponse response = commentService.incrementLikeCount(review.getId(), comment.getId());

        assertNotNull(response);
        assertEquals(comment.getId(), response.id());

        Comment updatedComment = Comment.builder()
            .id(comment.getId())
            .content(comment.getContent())
            .review(review)
            .likeCount(1) // Expected incremented value
            .dislikeCount(0)
            .build();
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(updatedComment));
        response = commentService.incrementLikeCount(review.getId(), comment.getId());

        assertEquals(1, response.likeCount());
        verify(commentRepository, times(2)).incrementLikeCount(comment.getId(), review.getId());
        verify(commentRepository, times(2)).findById(comment.getId());
    }

    @Test
    void incrementLikeCount_CommentNotFoundOrNotBelongingToReview_ThrowsResourceNotFoundException() {
        when(commentRepository.incrementLikeCount(comment.getId(), review.getId())).thenReturn(0);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            commentService.incrementLikeCount(review.getId(), comment.getId());
        });

        String expectedMessage = "Comment not found with id: " + comment.getId() + " for review id: " + review.getId() + " to increment like count.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
        verify(commentRepository, times(1)).incrementLikeCount(comment.getId(), review.getId());
        verify(commentRepository, never()).findById(anyLong());
    }

    @Test
    void incrementDislikeCount_Success() {
        when(commentRepository.incrementDislikeCount(comment.getId(), review.getId())).thenReturn(1);

        Comment dislikedComment = Comment.builder()
                .id(comment.getId())
                .content("Test comment")
                .review(review)
                .likeCount(0)
                .dislikeCount(1) // Dislike count incremented
                .build();
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(dislikedComment));

        CommentResponse response = commentService.incrementDislikeCount(review.getId(), comment.getId());

        assertNotNull(response);
        assertEquals(comment.getId(), response.id());
        assertEquals(1, response.dislikeCount());
        assertEquals(0, response.likeCount());

        verify(commentRepository, times(1)).incrementDislikeCount(comment.getId(), review.getId());
        verify(commentRepository, times(1)).findById(comment.getId());
    }

    @Test
    void incrementDislikeCount_CommentNotFoundOrNotBelongingToReview_ThrowsResourceNotFoundException() {
        when(commentRepository.incrementDislikeCount(comment.getId(), review.getId())).thenReturn(0);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            commentService.incrementDislikeCount(review.getId(), comment.getId());
        });

        String expectedMessage = "Comment not found with id: " + comment.getId() + " for review id: " + review.getId() + " to increment dislike count.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
        verify(commentRepository, times(1)).incrementDislikeCount(comment.getId(), review.getId());
        verify(commentRepository, never()).findById(anyLong());
    }

    @Test
    void createComment_whenParentIdIsNull_shouldCreateTopLevelComment() {
        // Arrange
        CommentCreateRequest request = new CommentCreateRequest("Test content"); // request.content() is "Test content"

        // Prepare the Comment object that we expect commentRepository.save() to return.
        // This object should reflect the data from the request.
        Comment commentToBeReturnedBySave = Comment.builder()
                .id(1L) // Using a consistent ID, e.g., from the 'comment' in setUp or a new one.
                .content(request.content()) // Content from the request
                .review(review) // review from setUp
                .likeCount(0)
                .dislikeCount(0)
                .build();

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        // Mock commentRepository.save to return the comment with the correct content
        when(commentRepository.save(any(Comment.class))).thenReturn(commentToBeReturnedBySave);

        // Act
        CommentResponse response = commentService.createComment(review.getId(), null, request);

        // Assert
        assertNotNull(response);
        assertEquals(commentToBeReturnedBySave.getId(), response.id());
        assertEquals(request.content(), response.content()); // Should now pass
        assertNull(response.parentId());

        verify(reviewRepository, times(1)).findById(review.getId());

        // Capture the argument passed to commentRepository.save and verify its properties
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository, times(1)).save(commentCaptor.capture());
        Comment commentPassedToSave = commentCaptor.getValue();
        assertEquals(request.content(), commentPassedToSave.getContent()); // Verify content passed to save
        assertEquals(review, commentPassedToSave.getReview());        // Verify review association
        assertNull(commentPassedToSave.getParent());                 // Verify parent is null

        verify(commentRepository, never()).findById(anyLong()); // No parent comment lookup, which is correct
    }

    @Test
    void createComment_whenParentIdIsNotNull_shouldCreateReplyComment() {
        // Arrange
        CommentCreateRequest request = new CommentCreateRequest("Reply content");
        Comment parentComment = Comment.builder().id(2L).content("Parent comment").review(review).build();
        Comment replyComment = Comment.builder().id(3L).content("Reply content").review(review).parent(parentComment).build();

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(commentRepository.findById(parentComment.getId())).thenReturn(Optional.of(parentComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(replyComment);

        // Act
        CommentResponse response = commentService.createComment(review.getId(), parentComment.getId(), request);

        // Assert
        assertNotNull(response);
        assertEquals(replyComment.getId(), response.id());
        assertEquals(request.content(), response.content());
        assertEquals(parentComment.getId(), response.parentId());
        verify(reviewRepository, times(1)).findById(review.getId());
        verify(commentRepository, times(1)).findById(parentComment.getId());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void getCommentsByReviewId_shouldReturnPageOfCommentResponses() {
        // Arrange
        Long reviewId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Comment> comments = Arrays.asList(
                Comment.builder().id(1L).content("Comment 1").review(review).build(),
                Comment.builder().id(2L).content("Comment 2").review(review).build()
        );
        Page<Comment> commentPage = new PageImpl<>(comments, pageable, comments.size());

        when(reviewRepository.existsById(reviewId)).thenReturn(true);
        when(commentRepository.findByReviewIdAndParentIsNull(reviewId, pageable)).thenReturn(commentPage);

        // Act
        Page<CommentResponse> resultPage = commentService.getCommentsByReviewId(reviewId, pageable);

        // Assert
        assertNotNull(resultPage);
        assertEquals(2, resultPage.getTotalElements());
        assertEquals(2, resultPage.getContent().size());
        assertEquals("Comment 1", resultPage.getContent().get(0).content());
        assertEquals("Comment 2", resultPage.getContent().get(1).content());

        verify(reviewRepository, times(1)).existsById(reviewId);
        verify(commentRepository, times(1)).findByReviewIdAndParentIsNull(reviewId, pageable);
    }

    @Test
    void getCommentsByReviewId_whenReviewNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        Long reviewId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        when(reviewRepository.existsById(reviewId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.getCommentsByReviewId(reviewId, pageable);
        });

        verify(reviewRepository, times(1)).existsById(reviewId);
        verify(commentRepository, never()).findByReviewIdAndParentIsNull(anyLong(), any(Pageable.class));
    }

    @Test
    void getRepliesOfComment_shouldReturnPageOfCommentResponses() { // Renamed method
        // Arrange
        Long reviewId = 1L; // Added reviewId
        Long parentCommentId = 1L;
        Pageable pageable = PageRequest.of(0, 5);
        List<Comment> replies = Arrays.asList(
                Comment.builder().id(2L).content("Reply 1").review(review).parent(comment).hasReplies(false).build(),
                Comment.builder().id(3L).content("Reply 2").review(review).parent(comment).hasReplies(false).build()
        );
        Page<Comment> replyPage = new PageImpl<>(replies, pageable, replies.size());

        when(reviewRepository.existsById(reviewId)).thenReturn(true); // Mock reviewRepository call
        when(commentRepository.existsById(parentCommentId)).thenReturn(true);
        when(commentRepository.findByParentId(parentCommentId, pageable)).thenReturn(replyPage);

        // Act
        Page<CommentResponse> resultPage = commentService.getRepliesOfComment(reviewId, parentCommentId, pageable); // Updated method call

        // Assert
        assertNotNull(resultPage);
        assertEquals(2, resultPage.getTotalElements());
        assertEquals("Reply 1", resultPage.getContent().get(0).content());
        assertFalse(resultPage.getContent().get(0).hasReplies()); // Assert hasReplies

        verify(reviewRepository, times(1)).existsById(reviewId); // Verify reviewRepository call
        verify(commentRepository, times(1)).existsById(parentCommentId);
        verify(commentRepository, times(1)).findByParentId(parentCommentId, pageable);
    }

    @Test
    void getRepliesOfComment_whenParentCommentNotFound_shouldThrowResourceNotFoundException() { // Renamed method
        // Arrange
        Long reviewId = 1L; // Added reviewId
        Long parentCommentId = 1L;
        Pageable pageable = PageRequest.of(0, 5);

        when(reviewRepository.existsById(reviewId)).thenReturn(true); // Mock reviewRepository call
        when(commentRepository.existsById(parentCommentId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.getRepliesOfComment(reviewId, parentCommentId, pageable); // Updated method call
        });

        verify(reviewRepository, times(1)).existsById(reviewId); // Verify reviewRepository call
        verify(commentRepository, times(1)).existsById(parentCommentId);
        verify(commentRepository, never()).findByParentId(anyLong(), any(Pageable.class));
    }

    @Test
    void getRepliesOfComment_whenReviewNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        Long reviewId = 1L;
        Long parentCommentId = 1L;
        Pageable pageable = PageRequest.of(0, 5);

        when(reviewRepository.existsById(reviewId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.getRepliesOfComment(reviewId, parentCommentId, pageable);
        });

        verify(reviewRepository, times(1)).existsById(reviewId);
        verify(commentRepository, never()).existsById(anyLong());
        verify(commentRepository, never()).findByParentId(anyLong(), any(Pageable.class));
    }

    @Test
    void createComment_shouldSetHasRepliesToFalseInitially() {
        // Arrange
        CommentCreateRequest request = new CommentCreateRequest("New comment content");
        Comment savedComment = Comment.builder()
                .id(10L)
                .content(request.content())
                .review(review)
                .likeCount(0)
                .dislikeCount(0)
                .status(com.incognito.reviewservice.model.CommentStatus.ACTIVE)
                .hasReplies(false) // Expect hasReplies to be false by default via @Formula or explicit set
                .build();

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        // Act
        CommentResponse response = commentService.createComment(review.getId(), null, request);

        // Assert
        assertNotNull(response);
        assertEquals(savedComment.getId(), response.id());
        assertEquals(request.content(), response.content());
        assertFalse(response.hasReplies(), "Newly created comment should not have replies indicated yet");

        // Verify that the saved entity (if we could inspect it directly before mapping)
        // would have hasReplies as false. The @Formula handles this at DB query time,
        // so direct assertion on the 'savedComment' mock's 'hasReplies' field is what we test in mapToCommentResponse.
        // Here, we primarily test the response DTO.
    }
}
