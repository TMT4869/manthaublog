package com.manthau.authservice.infrastructure.scheduler;

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

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherScheduler {

    private final OutboxEventJpaRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPending() {
        List<OutboxEventEntity> pending = outboxRepository.findByPublishedAtIsNullOrderByCreatedAtAsc();
        for (OutboxEventEntity event : pending) {
            try {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.USER_EXCHANGE,
                        event.getEventType(),
                        event.getPayload()
                );
                event.setPublishedAt(LocalDateTime.now());
                outboxRepository.save(event);
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}: {}", event.getId(), e.getMessage());
            }
        }
    }
}
