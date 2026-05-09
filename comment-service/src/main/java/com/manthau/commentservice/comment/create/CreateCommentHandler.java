package com.manthau.commentservice.comment.create;

import com.manthau.commentservice.comment.domain.Comment;
import com.manthau.commentservice.comment.domain.CommentRepository;
import com.manthau.commentservice.comment.event.CommentEventPublisher;
import com.manthau.commentservice.comment.list.CommentDto;
import com.manthau.commentservice.shared.exception.BadRequestException;
import com.manthau.commentservice.shared.exception.NotFoundException;
import com.manthau.commentservice.shared.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCommentHandler {

    private final CommentRepository commentRepository;
    private final CommentEventPublisher eventPublisher;

    @Transactional
    public CommentDto handle(CreateCommentRequest req, UserPrincipal user) {
        if (req.parentId() != null) {
            Comment parent = commentRepository.findById(req.parentId())
                    .orElseThrow(() -> new NotFoundException("Parent comment not found"));
            if (parent.isDeleted()) {
                throw new BadRequestException("Cannot reply to a deleted comment");
            }
            if (!parent.getPostId().equals(req.postId())) {
                throw new BadRequestException("Parent comment does not belong to the given post");
            }
        }

        Comment comment = Comment.builder()
                .postId(req.postId())
                .authorId(user.userId())
                .parentId(req.parentId())
                .content(req.content())
                .build();

        comment = commentRepository.save(comment);
        eventPublisher.publishCommentCreated(comment);
        return CommentDto.from(comment);
    }
}
