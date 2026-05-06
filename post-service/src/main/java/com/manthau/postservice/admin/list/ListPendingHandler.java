package com.manthau.postservice.admin.list;

import com.manthau.postservice.post.domain.PostRepository;
import com.manthau.postservice.post.domain.PostStatus;
import com.manthau.postservice.post.list.PostSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListPendingHandler {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public Page<PostSummaryDto> handle(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("submittedAt").ascending());
        return postRepository.findByStatus(PostStatus.pending_review, pageable)
                .map(PostSummaryDto::from);
    }
}
