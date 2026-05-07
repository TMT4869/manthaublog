package com.manthau.authservice.adapter.out.persistence;

import com.manthau.authservice.adapter.out.persistence.entity.OutboxEventEntity;
import com.manthau.authservice.adapter.out.persistence.jpa.OutboxEventJpaRepository;
import com.manthau.authservice.application.port.out.OutboxEventPort;
import com.manthau.authservice.domain.model.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventPersistenceAdapter implements OutboxEventPort {

    private final OutboxEventJpaRepository jpaRepository;

    @Override
    public void save(OutboxEvent event) {
        jpaRepository.save(OutboxEventEntity.builder()
                .aggregateType(event.getAggregateType())
                .aggregateId(event.getAggregateId())
                .eventType(event.getEventType())
                .payload(event.getPayload())
                .createdAt(event.getCreatedAt())
                .build());
    }
}
