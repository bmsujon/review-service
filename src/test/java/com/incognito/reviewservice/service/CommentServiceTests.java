package com.incognito.reviewservice.service;

import com.incognito.reviewservice.dto.CommentResponse;
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
        assertEquals(comment.getId(), response.getId());
        // Assuming the like count is incremented in the actual comment object by the service or repository method
        // For this test, we'd ideally verify the likeCount in the response if mapToCommentResponse reflects it
        // If incrementLikeCount directly modifies and returns the updated comment, this test is fine.
        // If it relies on findById fetching an updated comment, ensure the mock for findById returns the *expected* state.

        // Let's assume the comment object fetched by findById will have the updated count
        Comment updatedComment = Comment.builder()
            .id(comment.getId())
            .content(comment.getContent())
            .review(review)
            .likeCount(1) // Expected incremented value
            .dislikeCount(0)
            .build();
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(updatedComment));
        response = commentService.incrementLikeCount(review.getId(), comment.getId());


        assertEquals(1, response.getLikeCount());
        verify(commentRepository, times(2)).incrementLikeCount(comment.getId(), review.getId()); // Called twice due to re-setup for assertion
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
        // Simulate that the comment object will be refetched with an updated dislike count
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
        assertEquals(comment.getId(), response.getId());
        assertEquals(1, response.getDislikeCount()); // Verify dislike count
        assertEquals(0, response.getLikeCount());   // Like count should remain unchanged

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
}
