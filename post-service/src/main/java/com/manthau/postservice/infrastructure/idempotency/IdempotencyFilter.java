package com.manthau.postservice.infrastructure.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manthau.postservice.shared.security.UserPrincipal;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.cache.idempotency-ttl-hours:24}")
    private int idempotencyTtlHours;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        if (!StringUtils.hasText(idempotencyKey) || !HttpMethod.POST.matches(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        UUID userId = UserPrincipal.currentId();
        if (userId == null) {
            chain.doFilter(request, response);
            return;
        }

        String redisKey = "idem:" + userId + ":" + idempotencyKey;
        Object cached = redisTemplate.opsForValue().get(redisKey);
        if (cached != null) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(cached));
            return;
        }

        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        chain.doFilter(request, wrappedResponse);

        if (wrappedResponse.getStatus() < 400) {
            byte[] body = wrappedResponse.getContentAsByteArray();
            if (body.length > 0) {
                Object responseBody = objectMapper.readValue(body, Object.class);
                redisTemplate.opsForValue().set(redisKey, responseBody, Duration.ofHours(idempotencyTtlHours));
            }
        }
        wrappedResponse.copyBodyToResponse();
    }
}
