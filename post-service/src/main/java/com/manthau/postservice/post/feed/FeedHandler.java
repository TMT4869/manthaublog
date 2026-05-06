package com.manthau.postservice.post.feed;

import com.manthau.postservice.post.domain.PostRepository;
import com.manthau.postservice.post.list.PostSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedHandler {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public Page<PostSummaryDto> handle(List<UUID> followingAuthorIds, int page, int size) {
        if (followingAuthorIds == null || followingAuthorIds.isEmpty()) {
            return Page.empty();
        }
        return postRepository.findFeedForAuthors(followingAuthorIds, PageRequest.of(page, size))
                .map(PostSummaryDto::from);
    }
}
