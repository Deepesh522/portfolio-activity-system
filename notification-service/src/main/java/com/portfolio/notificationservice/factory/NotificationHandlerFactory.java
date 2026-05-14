package com.portfolio.notificationservice.factory;

import com.portfolio.notificationservice.repository.NotificationPreferenceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
  Factory Pattern implementation for resolving notification handlers.
  Extensible — new handlers are auto-discovered via Spring DI.
*/
@Component
@Slf4j
public class NotificationHandlerFactory {

    private final Map<String, NotificationChannelHandler> handlerMap;
    private final NotificationPreferenceRepository preferenceRepository;
    private final LogNotificationHandler logHandler;

    public NotificationHandlerFactory(List<NotificationChannelHandler> handlers,
            NotificationPreferenceRepository preferenceRepository,
            LogNotificationHandler logHandler) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(
                        h -> h.channel().toUpperCase(),
                        Function.identity()));
        this.preferenceRepository = preferenceRepository;
        this.logHandler = logHandler;

        log.info("Registered notification handlers: {}", handlerMap.keySet());
    }

    public List<NotificationChannelHandler> getHandlers(String userId) {
        List<NotificationChannelHandler> resolved = new ArrayList<>();
        resolved.add(logHandler); // Always log

        preferenceRepository.findByUserId(userId).ifPresent(pref -> {
            if (pref.isEmailEnabled()) {
                addHandler(resolved, "EMAIL");
            }
            if (pref.isSmsEnabled()) {
                addHandler(resolved, "SMS");
            }
            if (pref.isWebhookEnabled()) {
                addHandler(resolved, "WEBHOOK");
            }
        });

        return resolved;
    }

    public NotificationChannelHandler getHandler(String channel) {
        return handlerMap.getOrDefault(channel.toUpperCase(), logHandler);
    }

    private void addHandler(List<NotificationChannelHandler> list, String channel) {
        NotificationChannelHandler handler = handlerMap.get(channel);
        if (handler != null && !list.contains(handler)) {
            list.add(handler);
        }
    }
}
