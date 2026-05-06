package com.manthau.postservice.post.get;

import com.manthau.postservice.post.domain.Post;
import com.manthau.postservice.post.domain.PostRepository;
import com.manthau.postservice.post.domain.PostStatus;
import com.manthau.postservice.post.event.PostEventPublisher;
import com.manthau.postservice.post.event.PostViewEvent;
import com.manthau.postservice.shared.exception.ForbiddenException;
import com.manthau.postservice.shared.exception.NotFoundException;
import com.manthau.postservice.shared.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetPostHandler {

    private final PostRepository postRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PostEventPublisher eventPublisher;

    @Value("${app.cache.view-dedup-ttl-minutes:30}")
    private int viewDedupTtlMinutes;

    @Transactional(readOnly = true)
    public PostDetailDto handle(String slug, HttpServletRequest request) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        UserPrincipal user = UserPrincipal.current();
        if (post.getStatus() != PostStatus.published) {
            if (user == null || (!post.getAuthorId().equals(user.userId()) && !user.isAdmin())) {
                throw new ForbiddenException("Access denied");
            }
        }

        trackView(post, user, request);
        return PostDetailDto.from(post);
    }

    private void trackView(Post post, UserPrincipal user, HttpServletRequest request) {
        if (post.getStatus() != PostStatus.published) return;
        String identifier = user != null ? user.userId().toString() : ipHash(request);
        String dedupeKey = "view:dedup:" + post.getId() + ":" + identifier;
        Boolean isFirst = redisTemplate.opsForValue()
                .setIfAbsent(dedupeKey, "1", Duration.ofMinutes(viewDedupTtlMinutes));
        if (Boolean.TRUE.equals(isFirst)) {
            eventPublisher.publishViewTracked(new PostViewEvent(
                    post.getId(),
                    user != null ? user.userId() : null,
                    identifier
            ));
        }
    }

    private String ipHash(HttpServletRequest request) {
        try {
            String ip = request.getRemoteAddr();
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(ip.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (Exception e) {
            return "unknown";
        }
    }
}
