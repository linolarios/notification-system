package com.challenge.notification.dto.response;

import java.time.LocalDateTime;

public record NotificationJobResponse(
        String correlationId,
        Long jobId,
        Long messageId,
        String status,
        int attemptCount,
        String lastError,
        LocalDateTime createdAt,
        LocalDateTime processedAt
) {
}
