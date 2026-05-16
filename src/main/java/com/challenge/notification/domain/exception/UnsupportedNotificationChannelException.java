package com.challenge.notification.domain.exception;

import com.challenge.notification.domain.model.NotificationChannelCode;

public class UnsupportedNotificationChannelException extends RuntimeException {

    public UnsupportedNotificationChannelException(NotificationChannelCode channel) {
        super("Unsupported notification channel: " + channel);
    }
}
