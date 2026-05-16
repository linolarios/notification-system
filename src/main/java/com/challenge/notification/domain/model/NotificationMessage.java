package com.challenge.notification.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationMessage {
    private final Long id;
    private final String correlationId;
    private final CategoryCode category;
    private final String body;
    private final LocalDateTime createdAt;

    public NotificationMessage(CategoryCode category, String body) {
        this(null, UUID.randomUUID().toString(), category, body, null);
    }

    public NotificationMessage(Long id, String correlationId, CategoryCode category, String body, LocalDateTime createdAt) {
        this.id = id;
        this.correlationId = correlationId != null ? correlationId : UUID.randomUUID().toString();
        this.category = category;
        this.body = body;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public static NotificationMessage newMessage(CategoryCode categoryCode, @NotBlank(message = "message must not be blank") @Size(max = 1000, message = "message must not exceed 1000 characters") String body) {
        return new NotificationMessage(categoryCode, body);
    }

    public static NotificationMessage newMessage(String correlationId, CategoryCode categoryCode, String body) {
        return new NotificationMessage(
                null,
                correlationId,
                categoryCode,
                body,
                LocalDateTime.now()
        );
    }

    public Long getId() {
        return id;
    }

    public CategoryCode getCategory() {
        return category;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}
