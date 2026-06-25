package com.manthau.postservice.admin.list;

import com.manthau.postservice.post.list.PostSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ListPendingController {

    private final ListPendingHandler handler;

    @GetMapping("/api/admin/posts/pending")
    public Page<PostSummaryDto> listPending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return handler.handle(page, size);
    }
}
