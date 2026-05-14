package com.portfolio.notificationservice.factory;

/*
  Interface for notification delivery handlers.
  Each implementation handles a specific delivery channel (email, SMS, webhook, log).
  Extensible — add new channels by implementing this interface.
*/
public interface NotificationChannelHandler {

    /*
     * Deliver a notification to the user.
     * 
     * @param userId the target user
     * 
     * @param subject notification subject
     * 
     * @param message notification body
     */
    void send(String userId, String subject, String message);

    /*
     * Returns the channel name this handler supports.
     */
    String channel();
}
