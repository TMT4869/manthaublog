package com.manthau.searchservice.shared.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchanges (published by post-service)
    public static final String POST_APPROVED_EXCHANGE = "post.approved";
    public static final String POST_REJECTED_EXCHANGE = "post.rejected";
    public static final String POST_ARCHIVED_EXCHANGE = "post.archived";

    // Queues (consumed by search-service)
    public static final String POST_APPROVED_QUEUE = "search.post.approved.queue";
    public static final String POST_REJECTED_QUEUE = "search.post.rejected.queue";
    public static final String POST_ARCHIVED_QUEUE = "search.post.archived.queue";

    public static final String DLX = "dlx.exchange";
    public static final String DLX_ARG = "x-dead-letter-exchange";

    // --- Exchanges ---

    @Bean
    TopicExchange postApprovedExchange() {
        return new TopicExchange(POST_APPROVED_EXCHANGE);
    }

    @Bean
    TopicExchange postRejectedExchange() {
        return new TopicExchange(POST_REJECTED_EXCHANGE);
    }

    @Bean
    TopicExchange postArchivedExchange() {
        return new TopicExchange(POST_ARCHIVED_EXCHANGE);
    }

    // --- Queues ---

    @Bean
    Queue postApprovedQueue() {
        return QueueBuilder.durable(POST_APPROVED_QUEUE)
                .withArgument(DLX_ARG, DLX)
                .build();
    }

    @Bean
    Queue postRejectedQueue() {
        return QueueBuilder.durable(POST_REJECTED_QUEUE)
                .withArgument(DLX_ARG, DLX)
                .build();
    }

    @Bean
    Queue postArchivedQueue() {
        return QueueBuilder.durable(POST_ARCHIVED_QUEUE)
                .withArgument(DLX_ARG, DLX)
                .build();
    }

    // --- Bindings (# = receive all messages sent to these exchanges) ---

    @Bean
    Binding postApprovedBinding(Queue postApprovedQueue, TopicExchange postApprovedExchange) {
        return BindingBuilder.bind(postApprovedQueue).to(postApprovedExchange).with("#");
    }

    @Bean
    Binding postRejectedBinding(Queue postRejectedQueue, TopicExchange postRejectedExchange) {
        return BindingBuilder.bind(postRejectedQueue).to(postRejectedExchange).with("#");
    }

    @Bean
    Binding postArchivedBinding(Queue postArchivedQueue, TopicExchange postArchivedExchange) {
        return BindingBuilder.bind(postArchivedQueue).to(postArchivedExchange).with("#");
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
