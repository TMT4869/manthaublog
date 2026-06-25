package com.manthau.userservice.shared.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // User exchange & queues
    public static final String USER_EXCHANGE        = "user.exchange";
    public static final String USER_REGISTERED_QUEUE = "user.registered.queue";
    public static final String USER_FOLLOWED_QUEUE   = "user.followed.queue";

    // Post exchanges (consumed by this service)
    public static final String POST_APPROVED_EXCHANGE = "post.approved";
    public static final String POST_ARCHIVED_EXCHANGE = "post.archived";
    public static final String POST_APPROVED_QUEUE    = "user.post.approved.queue";
    public static final String POST_ARCHIVED_QUEUE    = "user.post.archived.queue";

    public static final String DLX = "dlx.exchange";
    public static final String KEY = "x-dead-letter-exchange";

    // --- Exchanges ---

    @Bean
    TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    @Bean
    TopicExchange postApprovedExchange() {
        return new TopicExchange(POST_APPROVED_EXCHANGE);
    }

    @Bean
    TopicExchange postArchivedExchange() {
        return new TopicExchange(POST_ARCHIVED_EXCHANGE);
    }

    // --- Queues ---

    @Bean
    Queue userRegisteredQueue() {
        return QueueBuilder.durable(USER_REGISTERED_QUEUE)
                .withArgument(KEY, DLX)
                .build();
    }

    @Bean
    Queue userFollowedQueue() {
        return QueueBuilder.durable(USER_FOLLOWED_QUEUE)
                .withArgument(KEY, DLX)
                .build();
    }

    @Bean
    Queue postApprovedQueue() {
        return QueueBuilder.durable(POST_APPROVED_QUEUE)
                .withArgument(KEY, DLX)
                .build();
    }

    @Bean
    Queue postArchivedQueue() {
        return QueueBuilder.durable(POST_ARCHIVED_QUEUE)
                .withArgument(KEY, DLX)
                .build();
    }

    // --- Bindings ---

    @Bean
    Binding userRegisteredBinding(Queue userRegisteredQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userRegisteredQueue).to(userExchange).with("user.registered");
    }

    @Bean
    Binding userFollowedBinding(Queue userFollowedQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userFollowedQueue).to(userExchange).with("user.followed");
    }

    @Bean
    Binding postApprovedBinding(Queue postApprovedQueue, TopicExchange postApprovedExchange) {
        return BindingBuilder.bind(postApprovedQueue).to(postApprovedExchange).with(POST_APPROVED_EXCHANGE);
    }

    @Bean
    Binding postArchivedBinding(Queue postArchivedQueue, TopicExchange postArchivedExchange) {
        return BindingBuilder.bind(postArchivedQueue).to(postArchivedExchange).with(POST_ARCHIVED_EXCHANGE);
    }

    // --- Infrastructure ---

    @Bean
    MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
