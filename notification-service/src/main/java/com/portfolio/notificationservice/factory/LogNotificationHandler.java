package com.portfolio.notificationservice.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LogNotificationHandler implements NotificationChannelHandler {

    @Override
    public void send(String userId, String subject, String message) {
        log.info("[LOG] Notification for {} | Subject: {} | Message: {}", userId, subject, message);
    }

    @Override
    public String channel() {
        return "LOG";
    }
}
