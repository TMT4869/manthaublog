package com.manthau.commentservice.comment.delete;

import com.manthau.commentservice.comment.domain.Comment;
import com.manthau.commentservice.comment.domain.CommentRepository;
import com.manthau.commentservice.shared.exception.ForbiddenException;
import com.manthau.commentservice.shared.exception.NotFoundException;
import com.manthau.commentservice.shared.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteCommentHandler {

    private final CommentRepository commentRepository;

    @Transactional
    public void handle(UUID commentId, UserPrincipal user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!comment.getAuthorId().equals(user.userId()) && !user.isAdmin()) {
            throw new ForbiddenException("You can only delete your own comments");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);
    }
}
