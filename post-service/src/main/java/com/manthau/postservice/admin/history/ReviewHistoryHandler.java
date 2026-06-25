package com.manthau.postservice.admin.history;

import com.manthau.postservice.admin.domain.PostReviewHistory;
import com.manthau.postservice.admin.domain.PostReviewHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewHistoryHandler {

    private final PostReviewHistoryRepository historyRepository;

    @Transactional(readOnly = true)
    public List<PostReviewHistory> handle(UUID postId) {
        return historyRepository.findByPostIdOrderByCreatedAtDesc(postId);
    }
}
