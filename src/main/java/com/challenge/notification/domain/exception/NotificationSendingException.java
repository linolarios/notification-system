package com.challenge.notification.domain.exception;

import com.challenge.notification.domain.model.NotificationChannelCode;

public class NotificationSendingException extends RuntimeException {

    public NotificationSendingException(NotificationChannelCode channel, String message) {
        super("Failed to send notification through channel " + channel + ": " + message);
    }

    public NotificationSendingException(NotificationChannelCode channel, Throwable cause) {
        super("Failed to send notification through channel " + channel, cause);
    }
}
