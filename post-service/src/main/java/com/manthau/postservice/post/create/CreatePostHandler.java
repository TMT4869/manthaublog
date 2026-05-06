package com.manthau.postservice.post.create;

import com.manthau.postservice.infrastructure.language.LanguageDetectionService;
import com.manthau.postservice.infrastructure.markdown.MarkdownRenderer;
import com.manthau.postservice.infrastructure.toc.TocBuilder;
import com.manthau.postservice.post.domain.Post;
import com.manthau.postservice.post.domain.PostRepository;
import com.manthau.postservice.post.get.PostDetailDto;
import com.manthau.postservice.shared.security.UserPrincipal;
import com.manthau.postservice.shared.util.ReadingTimeUtils;
import com.manthau.postservice.shared.util.SlugUtils;
import com.manthau.postservice.tag.domain.PostTag;
import com.manthau.postservice.tag.domain.PostTagId;
import com.manthau.postservice.tag.domain.Tag;
import com.manthau.postservice.tag.domain.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CreatePostHandler {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final MarkdownRenderer markdownRenderer;
    private final TocBuilder tocBuilder;
    private final LanguageDetectionService languageDetectionService;

    @Transactional
    public PostDetailDto handle(CreatePostRequest req, UserPrincipal user) {
        String slug = generateUniqueSlug(req.title());
        String html = markdownRenderer.render(req.content());
        List<Map<String, Object>> toc = tocBuilder.build(html);
        String lang = languageDetectionService.detect(req.content());
        int readingTime = ReadingTimeUtils.estimate(req.content());

        Post post = Post.builder()
                .authorId(user.userId())
                .title(req.title())
                .slug(slug)
                .content(req.content())
                .contentHtml(html)
                .excerpt(req.excerpt())
                .coverImageUrl(req.coverImageUrl())
                .language(lang)
                .toc(toc)
                .readingTimeMinutes((short) readingTime)
                .build();

        attachTags(post, req.tagSlugs());
        return PostDetailDto.from(postRepository.save(post));
    }

    private String generateUniqueSlug(String title) {
        String base = SlugUtils.slugify(title);
        String slug = base;
        int suffix = 1;
        while (postRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    private void attachTags(Post post, List<String> tagSlugs) {
        if (tagSlugs == null || tagSlugs.isEmpty()) return;
        List<Tag> tags = tagRepository.findBySlugIn(tagSlugs);
        tags.forEach(tag -> {
            PostTag pt = new PostTag(new PostTagId(post.getId(), tag.getId()), post, tag);
            post.getPostTags().add(pt);
        });
    }
}
