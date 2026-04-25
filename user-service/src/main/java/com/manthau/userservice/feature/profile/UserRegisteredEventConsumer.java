package com.manthau.userservice.feature.profile;

import com.manthau.userservice.shared.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegisteredEventConsumer {

    private final UserService userService;

    @RabbitListener(queues = RabbitMQConfig.USER_REGISTERED_QUEUE)
    public void handle(UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent for: {}", event.getUsername());
        userService.createProfile(event.getUserId(), event.getUsername(), event.getDisplayName());
    }
}