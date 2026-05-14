package com.portfolio.notificationservice.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmsNotificationHandler implements NotificationChannelHandler {

    @Override
    public void send(String userId, String subject, String message) {
        log.info("[SMS] To: {} | Message: {}", userId, message);
    }

    @Override
    public String channel() {
        return "SMS";
    }
}
