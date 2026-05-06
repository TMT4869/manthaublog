package com.manthau.postservice.shared.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchanges
    public static final String POST_SUBMITTED     = "post.submitted";
    public static final String POST_APPROVED      = "post.approved";
    public static final String POST_REJECTED      = "post.rejected";
    public static final String POST_ARCHIVED      = "post.archived";
    public static final String POST_VIEW_TRACKED  = "post.view.tracked";
    public static final String VIEW_COUNT_UPDATED = "view.count.updated";

    @Bean
    TopicExchange postSubmittedExchange()    { return new TopicExchange(POST_SUBMITTED); }
    @Bean
    TopicExchange postApprovedExchange()     { return new TopicExchange(POST_APPROVED); }
    @Bean
    TopicExchange postRejectedExchange()     { return new TopicExchange(POST_REJECTED); }
    @Bean
    TopicExchange postArchivedExchange()     { return new TopicExchange(POST_ARCHIVED); }
    @Bean
    TopicExchange postViewTrackedExchange()  { return new TopicExchange(POST_VIEW_TRACKED); }
    @Bean
    TopicExchange viewCountUpdatedExchange() { return new TopicExchange(VIEW_COUNT_UPDATED); }

    @Bean
    Queue viewCountUpdatedQueue() {
        return QueueBuilder.durable("post-service.view.count.updated").build();
    }

    @Bean
    Binding viewCountUpdatedBinding(Queue viewCountUpdatedQueue, TopicExchange viewCountUpdatedExchange) {
        return BindingBuilder.bind(viewCountUpdatedQueue).to(viewCountUpdatedExchange).with("#");
    }

    @Bean
    JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
