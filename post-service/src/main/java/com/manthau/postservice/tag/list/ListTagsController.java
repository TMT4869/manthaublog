package com.manthau.postservice.tag.list;

import com.manthau.postservice.tag.domain.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ListTagsController {

    private final ListTagsHandler handler;

    @GetMapping("/api/tags")
    public List<Tag> listTags() {
        return handler.handle();
    }
}
