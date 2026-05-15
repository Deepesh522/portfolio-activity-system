package com.portfolio.notificationservice.config;

import com.portfolio.shared.constant.CorrelationConstants;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RabbitCorrelationInterceptor implements MessagePostProcessor {

    @Override
    public Message postProcessMessage(Message message) {
        String correlationId = (String) message.getMessageProperties()
                .getHeaders().get(CorrelationConstants.CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(CorrelationConstants.CORRELATION_ID_MDC_KEY, correlationId);
        return message;
    }
}
