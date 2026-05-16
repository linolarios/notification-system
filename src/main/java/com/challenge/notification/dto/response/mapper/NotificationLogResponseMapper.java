package com.challenge.notification.dto.response.mapper;

import com.challenge.notification.dto.response.NotificationLogResponse;
import com.challenge.notification.infrastructure.persistence.entity.NotificationLogEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationLogResponseMapper {

    public NotificationLogResponse toNotificationLogResponse(NotificationLogEntity entity) {
        return new NotificationLogResponse(
                entity.getId(),
                entity.getCorrelationId(),
                entity.getMessageId(),
                entity.getUserId(),
                entity.getCategory().getCode(),
                entity.getChannel().getCode(),
                entity.getRecipientName(),
                entity.getRecipientEmail(),
                entity.getRecipientPhoneNumber(),
                entity.getStatus(),
                entity.getErrorMessage(),
                entity.getAttemptCount(),
                entity.getSentAt(),
                entity.getCreatedAt()
        );
    }
}
