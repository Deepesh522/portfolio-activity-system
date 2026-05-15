package com.portfolio.portfolioservice.config;

import com.portfolio.shared.constant.CorrelationConstants;
import org.slf4j.MDC;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.stereotype.Component;

@Component
public class RabbitCorrelationPostProcessor implements MessagePostProcessor {

    @Override
    public org.springframework.amqp.core.Message postProcessMessage(org.springframework.amqp.core.Message message) {
        String correlationId = MDC.get(CorrelationConstants.CORRELATION_ID_MDC_KEY);
        if (correlationId != null) {
            message.getMessageProperties().setHeader(CorrelationConstants.CORRELATION_ID_HEADER, correlationId);
        }
        return message;
    }
}
