package com.challenge.notification.domain.exception;

public class NotificationJobProcessingException extends RuntimeException {

    public NotificationJobProcessingException(String message) {
        super(message);
    }

    public NotificationJobProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
