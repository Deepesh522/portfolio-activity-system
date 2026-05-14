package com.portfolio.notificationservice.service;

import com.portfolio.notificationservice.entity.Notification;
import com.portfolio.notificationservice.factory.NotificationChannelHandler;
import com.portfolio.notificationservice.factory.NotificationHandlerFactory;
import com.portfolio.notificationservice.repository.NotificationRepository;
import com.portfolio.notificationservice.strategy.NotificationRuleStrategy;
import com.portfolio.shared.event.TransactionEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/*
Core notification processing service.
Evaluates all strategies, persists notifications,
and dispatches through resolved handlers.
*/
@Service
@Slf4j
public class NotificationService {

    private final List<NotificationRuleStrategy> ruleStrategies;
    private final NotificationHandlerFactory handlerFactory;
    private final NotificationRepository notificationRepository;
    private final Counter notificationsProcessed;
    private final Counter notificationsSent;
    private final Counter duplicatesSkipped;

    public NotificationService(List<NotificationRuleStrategy> ruleStrategies,
            NotificationHandlerFactory handlerFactory,
            NotificationRepository notificationRepository,
            MeterRegistry meterRegistry) {
        this.ruleStrategies = ruleStrategies;
        this.handlerFactory = handlerFactory;
        this.notificationRepository = notificationRepository;

        this.notificationsProcessed = Counter.builder("notifications.processed")
                .description("Total notification events processed")
                .register(meterRegistry);
        this.notificationsSent = Counter.builder("notifications.sent")
                .description("Total notifications successfully sent")
                .register(meterRegistry);
        this.duplicatesSkipped = Counter.builder("notifications.duplicates.skipped")
                .description("Duplicate events skipped via idempotency check")
                .register(meterRegistry);

        log.info("Initialized NotificationService with {} rule strategies: {}",
                ruleStrategies.size(),
                ruleStrategies.stream().map(NotificationRuleStrategy::ruleName).toList());
    }

    /*
     * Process a transaction event:
     * check idempotency->evaluate->rules->persist->dispatch.
     */
    @Transactional
    public void processEvent(TransactionEvent event) {
        notificationsProcessed.increment();

        // Idempotency check — skip if already processed
        if (notificationRepository.existsByEventId(event.eventId())) {
            log.warn("Duplicate event detected, skipping: eventId={}", event.eventId());
            duplicatesSkipped.increment();
            return;
        }

        log.info("Processing event: eventId={} userId={} symbol={} type={} totalValue={}",
                event.eventId(), event.userId(), event.symbol(), event.type(), event.totalValue());

        // Evaluate all strategies
        for (NotificationRuleStrategy rule : ruleStrategies) {
            if (rule.matches(event)) {
                log.info("Rule matched: {} for eventId={}", rule.ruleName(), event.eventId());
                String message = rule.generateMessage(event);
                String subject = rule.ruleName() + " Alert";

                // Persist notification
                Notification notification = Notification.builder()
                        .eventId(event.eventId())
                        .userId(event.userId())
                        .channel("MULTI")
                        .subject(subject)
                        .message(message)
                        .status(Notification.NotificationStatus.PENDING)
                        .ruleName(rule.ruleName())
                        .build();

                notification = notificationRepository.save(notification);

                // Dispatch using factory-resolved handlers
                List<NotificationChannelHandler> handlers = handlerFactory.getHandlers(event.userId());
                for (NotificationChannelHandler handler : handlers) {
                    try {
                        handler.send(event.userId(), subject, message);
                    } catch (Exception ex) {
                        log.error("Handler {} failed for eventId={}: {}",
                                handler.channel(), event.eventId(), ex.getMessage());
                    }
                }

                notification.setStatus(Notification.NotificationStatus.SENT);
                notification.setSentAt(Instant.now());
                notificationRepository.save(notification);

                notificationsSent.increment();

                return; // one notification per event
            }
        }
        log.debug("No rules matched for eventId={}", event.eventId());
    }
}
