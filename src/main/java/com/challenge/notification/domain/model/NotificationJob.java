package com.challenge.notification.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class NotificationJob {
    private static final int MAX_ERROR_LENGTH = 500;

    private final Long id;
    private final String correlationId;
    private final Long messageId;
    private final CategoryCode category;
    private final NotificationJobStatus status;
    private final int attemptCount;
    private final LocalDateTime lockedAt;
    private final LocalDateTime processedAt;
    private final String lastError;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private NotificationJob(Builder builder) {
        this.id = builder.id;
        this.correlationId = requireText(builder.correlationId, "correlationId");
        this.messageId = Objects.requireNonNull(builder.messageId, "messageId must not be null");
        this.category = Objects.requireNonNull(builder.category, "category must not be null");
        this.status = Objects.requireNonNull(builder.status, "status must not be null");
        this.attemptCount = validateAttemptCount(builder.attemptCount);
        this.lockedAt = builder.lockedAt;
        this.processedAt = builder.processedAt;
        this.lastError = truncateError(builder.lastError);
        this.createdAt = Objects.requireNonNull(builder.createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(builder.updatedAt, "updatedAt must not be null");

        validateState();
    }

    public static NotificationJob initializePending(Long messageId, String correlationId, CategoryCode category) {
        LocalDateTime now = LocalDateTime.now();

        return builder()
                .messageId(messageId)
                .correlationId(correlationId)
                .category(category)
                .status(NotificationJobStatus.PENDING)
                .attemptCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value;
    }

    public NotificationJob markProcessing() {
        ensureStatus(NotificationJobStatus.PENDING, "Only PENDING jobs can be marked as PROCESSING");

        LocalDateTime now = LocalDateTime.now();

        return toBuilder()
                .status(NotificationJobStatus.PROCESSING)
                .attemptCount(attemptCount + 1)
                .lockedAt(now)
                .processedAt(null)
                .lastError(null)
                .updatedAt(now)
                .build();
    }

    public NotificationJob markProcessed() {
        ensureStatus(NotificationJobStatus.PROCESSING, "Only PROCESSING jobs can be marked as PROCESSED");

        LocalDateTime now = LocalDateTime.now();

        return toBuilder()
                .status(NotificationJobStatus.PROCESSED)
                .processedAt(now)
                .lastError(null)
                .updatedAt(now)
                .build();
    }

    public NotificationJob markFailed(String errorMessage) {
        ensureStatus(NotificationJobStatus.PROCESSING, "Only PROCESSING jobs can be marked as FAILED");

        LocalDateTime now = LocalDateTime.now();

        return toBuilder()
                .status(NotificationJobStatus.FAILED)
                .processedAt(now)
                .lastError(errorMessage)
                .updatedAt(now)
                .build();
    }

    public NotificationJob resetToPendingForRetry() {
        ensureStatus(
                NotificationJobStatus.PROCESSING,
                "Only PROCESSING jobs can be reset to PENDING"
        );

        LocalDateTime now = LocalDateTime.now();

        return toBuilder()
                .status(NotificationJobStatus.PENDING)
                .lockedAt(null)
                .processedAt(null)
                .lastError(null)
                .updatedAt(now)
                .build();
    }

    private void ensureStatus(NotificationJobStatus expectedStatus, String message) {
        if (status != expectedStatus) {
            throw new IllegalStateException(message + ". Current status: " + status);
        }
    }

    private void validateState() {
        if (status == NotificationJobStatus.PENDING && processedAt != null) {
            throw new IllegalArgumentException("PENDING job must not have processedAt");
        }

        if (status == NotificationJobStatus.PROCESSING && lockedAt == null) {
            throw new IllegalArgumentException("PROCESSING job must have lockedAt");
        }

        if ((status == NotificationJobStatus.PROCESSED || status == NotificationJobStatus.FAILED)
                && processedAt == null) {
            throw new IllegalArgumentException(status + " job must have processedAt");
        }

        if (status == NotificationJobStatus.FAILED && (lastError == null || lastError.isBlank())) {
            throw new IllegalArgumentException("FAILED job must have lastError");
        }

        if (status != NotificationJobStatus.FAILED && lastError != null) {
            throw new IllegalArgumentException(status + " job must not have lastError");
        }
    }

    private static int validateAttemptCount(int attemptCount) {
        if (attemptCount < 0) {
            throw new IllegalArgumentException("attemptCount must not be negative");
        }

        return attemptCount;
    }

    private static String truncateError(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }

        return errorMessage.length() <= MAX_ERROR_LENGTH
                ? errorMessage
                : errorMessage.substring(0, MAX_ERROR_LENGTH);
    }

    public Builder toBuilder() {
        return builder()
                .id(id)
                .messageId(messageId)
                .correlationId(correlationId)
                .category(category)
                .status(status)
                .attemptCount(attemptCount)
                .lockedAt(lockedAt)
                .processedAt(processedAt)
                .lastError(lastError)
                .createdAt(createdAt)
                .updatedAt(updatedAt);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public String getCorrelationId() { return correlationId;}

    public Long getMessageId() {
        return messageId;
    }

    public CategoryCode getCategory() {
        return category;
    }

    public NotificationJobStatus getStatus() {
        return status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public LocalDateTime getLockedAt() {
        return lockedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public String getLastError() {
        return lastError;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static class Builder {

        private Long id;
        private String correlationId;
        private Long messageId;
        private CategoryCode category;
        private NotificationJobStatus status = NotificationJobStatus.PENDING;
        private int attemptCount;
        private LocalDateTime lockedAt;
        private LocalDateTime processedAt;
        private String lastError;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder messageId(Long messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder category(CategoryCode category) {
            this.category = category;
            return this;
        }

        public Builder status(NotificationJobStatus status) {
            this.status = status;
            return this;
        }

        public Builder attemptCount(int attemptCount) {
            this.attemptCount = attemptCount;
            return this;
        }

        public Builder lockedAt(LocalDateTime lockedAt) {
            this.lockedAt = lockedAt;
            return this;
        }

        public Builder processedAt(LocalDateTime processedAt) {
            this.processedAt = processedAt;
            return this;
        }

        public Builder lastError(String lastError) {
            this.lastError = lastError;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public NotificationJob build() {
            return new NotificationJob(this);
        }
    }
}
