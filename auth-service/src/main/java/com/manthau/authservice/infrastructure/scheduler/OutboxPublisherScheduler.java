package com.manthau.authservice.infrastructure.scheduler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manthau.authservice.adapter.out.persistence.entity.OutboxEventEntity;
import com.manthau.authservice.adapter.out.persistence.jpa.OutboxEventJpaRepository;
import com.manthau.authservice.infrastructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherScheduler {

    private final OutboxEventJpaRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPending() {
        List<OutboxEventEntity> pending = outboxRepository.findByPublishedAtIsNullOrderByCreatedAtAsc();
        for (OutboxEventEntity event : pending) {
            try {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.USER_EXCHANGE,
                        event.getEventType(),
                        objectMapper.readValue(event.getPayload(), new TypeReference<Map<String, Object>>() {})
                );
                event.setPublishedAt(LocalDateTime.now());
                outboxRepository.save(event);
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}: {}", event.getId(), e.getMessage());
            }
        }
    }
}
