package com.manthau.commentservice.comment.list;

import com.manthau.commentservice.comment.domain.Comment;
import com.manthau.commentservice.comment.domain.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ListCommentsHandler {

    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public List<CommentDto> handle(UUID postId) {
        List<Comment> flat = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        return buildTree(flat);
    }

    private List<CommentDto> buildTree(List<Comment> flat) {
        Map<UUID, CommentDto> byId = new LinkedHashMap<>();
        for (Comment c : flat) {
            byId.put(c.getId(), CommentDto.from(c));
        }

        List<CommentDto> roots = new ArrayList<>();
        for (CommentDto dto : byId.values()) {
            if (dto.parentId() == null) {
                roots.add(dto);
            } else {
                CommentDto parent = byId.get(dto.parentId());
                if (parent != null) {
                    parent.replies().add(dto);
                } else {
                    // orphaned reply (parent was hard-deleted) — attach to root level
                    roots.add(dto);
                }
            }
        }
        return roots;
    }
}
