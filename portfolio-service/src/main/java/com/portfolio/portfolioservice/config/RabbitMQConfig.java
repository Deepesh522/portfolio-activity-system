package com.portfolio.portfolioservice.config;

import com.portfolio.shared.constant.MessagingConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for the Portfolio Service.
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange portfolioExchange() {
        return ExchangeBuilder.topicExchange(MessagingConstants.PORTFOLIO_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(MessagingConstants.NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", MessagingConstants.NOTIFICATION_DLQ)
                .build();
    }

    @Bean
    public Queue notificationRetryQueue() {
        return QueueBuilder.durable(MessagingConstants.NOTIFICATION_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", MessagingConstants.NOTIFICATION_QUEUE)
                .withArgument("x-message-ttl", MessagingConstants.RETRY_INITIAL_INTERVAL_MS)
                .build();
    }

    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(MessagingConstants.NOTIFICATION_DLQ).build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(portfolioExchange())
                .with(MessagingConstants.PORTFOLIO_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
