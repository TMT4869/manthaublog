package com.manthau.postservice.admin.history;

import com.manthau.postservice.admin.domain.PostReviewHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ReviewHistoryController {

    private final ReviewHistoryHandler handler;

    @GetMapping("/api/admin/posts/{id}/history")
    public List<PostReviewHistory> getHistory(@PathVariable UUID id) {
        return handler.handle(id);
    }
}
