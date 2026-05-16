package com.challenge.notification.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs")
public class NotificationLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "correlation_id", nullable = false, length = 80)
    private String correlationId;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channel_id", nullable = false)
    private NotificationChannelEntity channel;

    @Column(name = "recipient_name", nullable = false, length = 120)
    private String recipientName;

    @Column(name = "recipient_email", length = 180)
    private String recipientEmail;

    @Column(name = "recipient_phone_number", length = 30)
    private String recipientPhoneNumber;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected NotificationLogEntity() {
    }

    public NotificationLogEntity(
            Long id,
            String correlationId,
            Long messageId,
            Long userId,
            CategoryEntity category,
            NotificationChannelEntity channel,
            String recipientName,
            String recipientEmail,
            String recipientPhoneNumber,
            String status,
            String errorMessage,
            int attemptCount,
            LocalDateTime sentAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.correlationId = correlationId;
        this.messageId = messageId;
        this.userId = userId;
        this.category = category;
        this.channel = channel;
        this.recipientName = recipientName;
        this.recipientEmail = recipientEmail;
        this.recipientPhoneNumber = recipientPhoneNumber;
        this.status = status;
        this.errorMessage = errorMessage;
        this.attemptCount = attemptCount;
        this.sentAt = sentAt;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getCorrelationId() {return correlationId;}

    public Long getMessageId() {
        return messageId;
    }

    public Long getUserId() {
        return userId;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public NotificationChannelEntity getChannel() {
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

    public String getStatus() {
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
}
