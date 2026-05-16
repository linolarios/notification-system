package com.challenge.notification.dto.response;

import com.challenge.notification.infrastructure.persistence.entity.NotificationLogEntity;

import java.time.LocalDateTime;

public record NotificationLogResponse(
        Long id,
        String correlationId,
        Long messageId,
        Long userId,
        String category,
        String channel,
        String recipientName,
        String recipientEmail,
        String recipientPhoneNumber,
        String status,
        String errorMessage,
        int attemptCount,
        LocalDateTime sentAt,
        LocalDateTime createdAt
) {
}
