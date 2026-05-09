package com.manthau.commentservice.comment.update;

import com.manthau.commentservice.comment.domain.Comment;
import com.manthau.commentservice.comment.domain.CommentRepository;
import com.manthau.commentservice.comment.list.CommentDto;
import com.manthau.commentservice.shared.exception.BadRequestException;
import com.manthau.commentservice.shared.exception.ForbiddenException;
import com.manthau.commentservice.shared.exception.NotFoundException;
import com.manthau.commentservice.shared.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateCommentHandler {

    private final CommentRepository commentRepository;

    @Transactional
    public CommentDto handle(UUID commentId, UpdateCommentRequest req, UserPrincipal user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (comment.isDeleted()) {
            throw new BadRequestException("Cannot edit a deleted comment");
        }
        if (!comment.getAuthorId().equals(user.userId())) {
            throw new ForbiddenException("You can only edit your own comments");
        }

        comment.setContent(req.content());
        return CommentDto.from(commentRepository.save(comment));
    }
}
