package com.challenge.notification.domain.model;

import java.time.LocalDateTime;
import java.util.Optional;

public class NotificationSendResult {
    private final boolean success;
    private final String providerMessageId;
    private final String errorMessage;
    private final LocalDateTime sentAt;

    private NotificationSendResult(
            boolean success,
            String providerMessageId,
            String errorMessage,
            LocalDateTime sentAt
    ) {
        this.success = success;
        this.providerMessageId = providerMessageId;
        this.errorMessage = errorMessage;
        this.sentAt = sentAt;
    }

    public static NotificationSendResult success(String providerMessageId) {
        return new NotificationSendResult(
                true,
                providerMessageId,
                null,
                LocalDateTime.now()
        );
    }

    public static NotificationSendResult failure(String errorMessage) {
        return new NotificationSendResult(
                false,
                null,
                errorMessage,
                null
        );
    }

    public boolean isSuccess() {
        return success;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public Optional<String> getErrorMessage() { return Optional.ofNullable(errorMessage); }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}
