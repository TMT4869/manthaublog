package com.manthau.authservice.application.port.out;

import com.manthau.authservice.domain.model.OutboxEvent;

public interface OutboxEventPort {
    void save(OutboxEvent event);
}
