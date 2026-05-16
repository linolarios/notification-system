package com.challenge.notification.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class NotificationDeliveryAttempt {

    public static final int FIRST_ATTEMPT_COUNT = 1;
    private static final int MAX_ERROR_LENGTH = 500;
    private final Long id;
    private final String correlationId;
    private final Long messageId;
    private final Long userId;
    private final CategoryCode category;
    private final NotificationChannelCode channel;
    private final String recipientName;
    private final String recipientEmail;
    private final String recipientPhoneNumber;
    private final NotificationStatus status;
    private final String errorMessage;
    private final int attemptCount;
    private final LocalDateTime sentAt;
    private final LocalDateTime createdAt;

    private NotificationDeliveryAttempt(Builder builder) {
        this.id = builder.id;
        this.correlationId = requireText(builder.correlationId, "correlationId");
        this.messageId = Objects.requireNonNull(builder.messageId, "messageId must not be null");
        this.userId = Objects.requireNonNull(builder.userId, "userId must not be null");
        this.category = Objects.requireNonNull(builder.category, "category must not be null");
        this.channel = Objects.requireNonNull(builder.channel, "channel must not be null");
        this.recipientName = requireText(builder.recipientName, "recipientName");
        this.recipientEmail = normalizeOptionalText(builder.recipientEmail);
        this.recipientPhoneNumber = normalizeOptionalText(builder.recipientPhoneNumber);
        this.status = Objects.requireNonNull(builder.status, "status must not be null");
        this.errorMessage = truncateError(builder.errorMessage);
        this.attemptCount = validateAttemptCount(builder.attemptCount);
        this.sentAt = builder.sentAt;
        this.createdAt = Objects.requireNonNull(builder.createdAt, "createdAt must not be null");

        validateState();
    }

    public static NotificationDeliveryAttempt sent(
            NotificationMessage message,
            NotificationSubscriber subscriber,
            NotificationChannelCode channel,
            NotificationSendResult result
    ) {
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(subscriber, "subscriber must not be null");
        Objects.requireNonNull(result, "result must not be null");

        LocalDateTime sentAt = result.getSentAt() != null
                ? result.getSentAt()
                : LocalDateTime.now();

        return builder()
                .correlationId(message.getCorrelationId())
                .messageId(message.getId())
                .userId(subscriber.getId())
                .category(message.getCategory())
                .channel(channel)
                .recipientName(subscriber.getName())
                .recipientEmail(subscriber.getEmail())
                .recipientPhoneNumber(subscriber.getPhoneNumber())
                .status(NotificationStatus.SENT)
                .attemptCount(FIRST_ATTEMPT_COUNT)
                .sentAt(sentAt)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static NotificationDeliveryAttempt failed(
            NotificationMessage message,
            NotificationSubscriber subscriber,
            NotificationChannelCode channel,
            String errorMessage
    ) {
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(subscriber, "subscriber must not be null");

        return builder()
                .correlationId(message.getCorrelationId())
                .messageId(message.getId())
                .userId(subscriber.getId())
                .category(message.getCategory())
                .channel(channel)
                .recipientName(subscriber.getName())
                .recipientEmail(subscriber.getEmail())
                .recipientPhoneNumber(subscriber.getPhoneNumber())
                .status(NotificationStatus.FAILED)
                .errorMessage(errorMessage)
                .attemptCount(1)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    private static int validateAttemptCount(int attemptCount) {
        if (attemptCount < FIRST_ATTEMPT_COUNT) {
            throw new IllegalArgumentException("attemptCount must be greater than or equal to 1");
        }

        return attemptCount;
    }

    private static String requireText(String value, String fieldName) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value;
    }

    private static String normalizeOptionalText(String value) {
        if (isBlank(value)) {
            return null;
        }

        return value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String truncateError(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }

        return errorMessage.length() <= MAX_ERROR_LENGTH
                ? errorMessage
                : errorMessage.substring(0, MAX_ERROR_LENGTH);
    }

    public NotificationDeliveryAttempt withIncrementedAttemptCount() {
        return toBuilder()
                .attemptCount(attemptCount + 1)
                .build();
    }

    public Builder toBuilder() {
        return builder()
                .id(id)
                .correlationId(correlationId)
                .messageId(messageId)
                .userId(userId)
                .category(category)
                .channel(channel)
                .recipientName(recipientName)
                .recipientEmail(recipientEmail)
                .recipientPhoneNumber(recipientPhoneNumber)
                .status(status)
                .errorMessage(errorMessage)
                .attemptCount(attemptCount)
                .sentAt(sentAt)
                .createdAt(createdAt);
    }

    private void validateState() {
        if (status == NotificationStatus.SENT && sentAt == null) {
            throw new IllegalArgumentException("SENT delivery attempt must have sentAt");
        }

        if (status == NotificationStatus.SENT && errorMessage != null) {
            throw new IllegalArgumentException("SENT delivery attempt must not have errorMessage");
        }

        if (status == NotificationStatus.FAILED && (errorMessage == null || errorMessage.isBlank())) {
            throw new IllegalArgumentException("FAILED delivery attempt must have errorMessage");
        }

        if (status == NotificationStatus.PENDING && sentAt != null) {
            throw new IllegalArgumentException("PENDING delivery attempt must not have sentAt");
        }

        if (status == NotificationStatus.PENDING && errorMessage != null) {
            throw new IllegalArgumentException("PENDING delivery attempt must not have errorMessage");
        }

        validateRecipientForChannel();
    }

    private void validateRecipientForChannel() {
        if (channel == NotificationChannelCode.EMAIL && isBlank(recipientEmail)) {
            throw new IllegalArgumentException("EMAIL delivery attempt requires recipientEmail");
        }

        if (channel == NotificationChannelCode.SMS && isBlank(recipientPhoneNumber)) {
            throw new IllegalArgumentException("SMS delivery attempt requires recipientPhoneNumber");
        }
    }

    public Long getId() {
        return id;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public Long getUserId() {
        return userId;
    }

    public CategoryCode getCategory() {
        return category;
    }

    public NotificationChannelCode getChannel() {
        return channel;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getRecipientPhoneNumber() {
        return recipientPhoneNumber;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public static class Builder {

        private Long id;
        private String correlationId;
        private Long messageId;
        private Long userId;
        private CategoryCode category;
        private NotificationChannelCode channel;
        private String recipientName;
        private String recipientEmail;
        private String recipientPhoneNumber;
        private NotificationStatus status = NotificationStatus.PENDING;
        private String errorMessage;
        private int attemptCount = 1;
        private LocalDateTime sentAt;
        private LocalDateTime createdAt = LocalDateTime.now();

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

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder category(CategoryCode category) {
            this.category = category;
            return this;
        }

        public Builder channel(NotificationChannelCode channel) {
            this.channel = channel;
            return this;
        }

        public Builder recipientName(String recipientName) {
            this.recipientName = recipientName;
            return this;
        }

        public Builder recipientEmail(String recipientEmail) {
            this.recipientEmail = recipientEmail;
            return this;
        }

        public Builder recipientPhoneNumber(String recipientPhoneNumber) {
            this.recipientPhoneNumber = recipientPhoneNumber;
            return this;
        }

        public Builder status(NotificationStatus status) {
            this.status = status;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder attemptCount(int attemptCount) {
            this.attemptCount = attemptCount;
            return this;
        }

        public Builder sentAt(LocalDateTime sentAt) {
            this.sentAt = sentAt;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public NotificationDeliveryAttempt build() {
            return new NotificationDeliveryAttempt(this);
        }
    }
}