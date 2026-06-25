package com.manthau.postservice.tag.list;

import com.manthau.postservice.tag.domain.Tag;
import com.manthau.postservice.tag.domain.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListTagsHandler {

    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public List<Tag> handle() {
        return tagRepository.findTop20ByOrderByPostsCountDesc();
    }
}
