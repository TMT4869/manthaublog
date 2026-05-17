package com.manthau.postservice.post.update;

import com.manthau.postservice.category.domain.Category;
import com.manthau.postservice.category.domain.CategoryRepository;
import com.manthau.postservice.infrastructure.language.LanguageDetectionService;
import com.manthau.postservice.infrastructure.markdown.MarkdownRenderer;
import com.manthau.postservice.infrastructure.toc.TocBuilder;
import com.manthau.postservice.post.domain.Post;
import com.manthau.postservice.post.domain.PostRepository;
import com.manthau.postservice.post.domain.PostStatus;
import com.manthau.postservice.post.get.PostDetailDto;
import com.manthau.postservice.shared.exception.BadRequestException;
import com.manthau.postservice.shared.exception.ForbiddenException;
import com.manthau.postservice.shared.exception.NotFoundException;
import com.manthau.postservice.shared.security.UserPrincipal;
import com.manthau.postservice.shared.util.ReadingTimeUtils;
import com.manthau.postservice.tag.domain.PostTag;
import com.manthau.postservice.tag.domain.PostTagId;
import com.manthau.postservice.tag.domain.Tag;
import com.manthau.postservice.tag.domain.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UpdatePostHandler {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final MarkdownRenderer markdownRenderer;
    private final TocBuilder tocBuilder;
    private final LanguageDetectionService languageDetectionService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public PostDetailDto handle(UUID postId, UpdatePostRequest req, UserPrincipal user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        if (!post.getAuthorId().equals(user.userId())) {
            throw new ForbiddenException("Not the post owner");
        }
        if (post.getStatus() == PostStatus.published || post.getStatus() == PostStatus.archived) {
            throw new BadRequestException("Cannot edit published or archived post");
        }

        String html = markdownRenderer.render(req.content());
        List<Map<String, Object>> toc = tocBuilder.build(html);
        String lang = languageDetectionService.detect(req.content());
        int readingTime = ReadingTimeUtils.estimate(req.content());
        Category category = findCategory(req.categorySlug());

        post.setTitle(req.title());
        post.setContent(req.content());
        post.setContentHtml(html);
        post.setExcerpt(req.excerpt());
        post.setCoverImageUrl(req.coverImageUrl());
        post.setCategory(category);
        post.setLanguage(lang);
        post.setToc(toc);
        post.setReadingTimeMinutes((short) readingTime);

        post.getPostTags().clear();
        attachTags(post, req.tagSlugs());

        Post saved = postRepository.save(post);
        redisTemplate.delete("post:" + postId);
        return PostDetailDto.from(saved);
    }

    private Category findCategory(String categorySlug) {
        return categoryRepository.findBySlug(categorySlug)
                .orElseThrow(() -> new NotFoundException("Category not found"));
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
