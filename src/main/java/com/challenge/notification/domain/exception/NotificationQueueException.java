package com.challenge.notification.domain.exception;

public class NotificationQueueException extends RuntimeException {

    public NotificationQueueException(String message) {
        super(message);
    }

    public NotificationQueueException(String message, Throwable cause) {
        super(message, cause);
    }
}
