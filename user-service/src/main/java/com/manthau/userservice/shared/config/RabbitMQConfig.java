package com.manthau.userservice.shared.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitMQConfig {

    public static final String USER_EXCHANGE = "user.exchange";
    public static final String USER_REGISTERED_QUEUE = "user.registered.queue";
    public static final String USER_FOLLOWED_QUEUE = "user.followed.queue";
    public static final String POST_APPROVED_QUEUE = "user.post.approved.queue";
    public static final String POST_ARCHIVED_QUEUE = "user.post.archived.queue";
    public static final String KEY = "x-dead-letter-exchange";
    public static final String DLX = "dlx.exchange";

    @Bean
    TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

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
    Binding userRegisteredBinding(Queue userRegisteredQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userRegisteredQueue).to(userExchange).with("user.registered");
    }

    @Bean
    Binding userFollowedBinding(Queue userFollowedQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userFollowedQueue).to(userExchange).with("user.followed");
    }

    @Bean
    Queue postApprovedQueue() {
        return QueueBuilder.durable(POST_APPROVED_QUEUE)
                .withArgument(KEY, DLX)
                .build();
    }

    @Bean
    Binding postApprovedBinding(TopicExchange postExchange) {
        return BindingBuilder.bind(postApprovedQueue()).to(postExchange).with("post.approved");
    }

    @Bean
    MessageConverter messageConverter() {
        JsonMapper jsonMapper = JsonMapper.builder().build();
        return new JacksonJsonMessageConverter(jsonMapper);
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}