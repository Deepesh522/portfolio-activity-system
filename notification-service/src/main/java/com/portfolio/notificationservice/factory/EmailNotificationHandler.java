package com.portfolio.notificationservice.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailNotificationHandler implements NotificationChannelHandler {

    @Override
    public void send(String userId, String subject, String message) {
        log.info("[EMAIL] To: {} | Subject: {} | Body: {}", userId, subject, message);
    }

    @Override
    public String channel() {
        return "EMAIL";
    }
}
