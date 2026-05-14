package com.portfolio.notificationservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.notificationservice.entity.FailedNotification;
import com.portfolio.notificationservice.repository.FailedNotificationRepository;
import com.portfolio.notificationservice.service.NotificationService;
import com.portfolio.shared.constant.CorrelationConstants;
import com.portfolio.shared.constant.MessagingConstants;
import com.portfolio.shared.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

// RabbitMQ consumer for transaction events.

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final NotificationService notificationService;
    private final RabbitTemplate rabbitTemplate;
    private final FailedNotificationRepository failedNotificationRepository;
    private final ObjectMapper objectMapper;

    /*
     * Main consumer — listens to notification.queue.
     * On failure: routes to retry queue (up to MAX_RETRY_COUNT), then to DLQ.
     */
    @RabbitListener(queues = MessagingConstants.NOTIFICATION_QUEUE, concurrency = "3-5")
    public void consume(TransactionEvent event, Message message) {
        String correlationId = getHeaderValue(message, CorrelationConstants.CORRELATION_ID_HEADER);
        if (correlationId != null) {
            MDC.put(CorrelationConstants.CORRELATION_ID_MDC_KEY, correlationId);
        }

        int retryCount = getRetryCount(message);

        try {
            log.info("Received transaction event: eventId={} userId={} symbol={} retry={}",
                    event.eventId(), event.userId(), event.symbol(), retryCount);

            notificationService.processEvent(event);

            log.info("Event processed successfully: eventId={}", event.eventId());
        } catch (Exception ex) {
            log.error("Error processing event: eventId={} retry={} error={}",
                    event.eventId(), retryCount, ex.getMessage(), ex);

            handleFailure(event, message, retryCount, ex);
        } finally {
            MDC.remove(CorrelationConstants.CORRELATION_ID_MDC_KEY);
        }
    }

    /*
     * DLQ listener — persists permanently failed events for manual investigation.
     */
    @RabbitListener(queues = MessagingConstants.NOTIFICATION_DLQ)
    public void consumeDlq(TransactionEvent event, Message message) {
        log.error("DLQ received permanently failed event: eventId={} userId={}",
                event.eventId(), event.userId());

        try {
            String payload = objectMapper.writeValueAsString(event);
            FailedNotification failed = FailedNotification.builder()
                    .eventId(event.eventId())
                    .userId(event.userId())
                    .payload(payload)
                    .failureReason("Exhausted all retry attempts")
                    .retryCount(MessagingConstants.MAX_RETRY_COUNT)
                    .build();

            failedNotificationRepository.save(failed);
            log.info("Persisted failed notification for manual review: eventId={}", event.eventId());
        } catch (JsonProcessingException ex) {
            log.error("Failed to serialize DLQ event: eventId={}", event.eventId(), ex);
        }
    }

    private void handleFailure(TransactionEvent event, Message message,
            int retryCount, Exception ex) {
        if (retryCount < MessagingConstants.MAX_RETRY_COUNT) {
            log.warn("Routing to retry queue: eventId={} attempt={}/{}",
                    event.eventId(), retryCount + 1, MessagingConstants.MAX_RETRY_COUNT);

            message.getMessageProperties().setHeader("x-retry-count", retryCount + 1);
            rabbitTemplate.send("", MessagingConstants.NOTIFICATION_RETRY_QUEUE, message);
        } else {
            log.error("Max retries exhausted, routing to DLQ: eventId={}", event.eventId());
            rabbitTemplate.send("", MessagingConstants.NOTIFICATION_DLQ, message);
        }
    }

    private int getRetryCount(Message message) {
        Object retryHeader = message.getMessageProperties().getHeader("x-retry-count");
        if (retryHeader instanceof Integer count) {
            return count;
        }

        // Check for RabbitMQ native x-death header
        List<Map<String, ?>> xDeath = message.getMessageProperties().getHeader("x-death");
        if (xDeath != null && !xDeath.isEmpty()) {
            Object count = xDeath.getFirst().get("count");
            if (count instanceof Long l) {
                return l.intValue();
            }
        }
        return 0;
    }

    private String getHeaderValue(Message message, String headerName) {
        Object value = message.getMessageProperties().getHeader(headerName);
        return value != null ? value.toString() : null;
    }
}
