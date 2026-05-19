package com.challenge.notification.domain.exception;

import com.challenge.notification.domain.model.NotificationChannelCode;

/**
 * Raised when no notification sender is registered for the requested channel.
 */
public class UnsupportedNotificationChannelException extends RuntimeException {

    public UnsupportedNotificationChannelException(NotificationChannelCode channel) {
        super("Unsupported notification channel: " + channel);
    }

    public UnsupportedNotificationChannelException(String channel) {
        super("Unsupported notification channel: " + channel);
    }
}
