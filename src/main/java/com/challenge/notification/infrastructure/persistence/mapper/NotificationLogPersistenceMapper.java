package com.challenge.notification.infrastructure.persistence.mapper;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationChannelCode;
import com.challenge.notification.domain.model.NotificationDeliveryAttempt;
import com.challenge.notification.domain.model.NotificationStatus;
import com.challenge.notification.infrastructure.persistence.entity.CategoryEntity;
import com.challenge.notification.infrastructure.persistence.entity.NotificationChannelEntity;
import com.challenge.notification.infrastructure.persistence.entity.NotificationLogEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationLogPersistenceMapper {

    public NotificationLogEntity toEntity(
            NotificationDeliveryAttempt deliveryAttempt,
            CategoryEntity categoryEntity,
            NotificationChannelEntity channelEntity
    ) {
        return new NotificationLogEntity(
                deliveryAttempt.getId(),
                deliveryAttempt.getCorrelationId(),
                deliveryAttempt.getMessageId(),
                deliveryAttempt.getUserId(),
                categoryEntity,
                channelEntity,
                deliveryAttempt.getRecipientName(),
                deliveryAttempt.getRecipientEmail(),
                deliveryAttempt.getRecipientPhoneNumber(),
                deliveryAttempt.getStatus().name(),
                deliveryAttempt.getErrorMessage(),
                deliveryAttempt.getAttemptCount(),
                deliveryAttempt.getSentAt(),
                deliveryAttempt.getCreatedAt()
        );
    }

    public NotificationDeliveryAttempt toDomain(NotificationLogEntity entity) {
        return NotificationDeliveryAttempt.builder()
                .id(entity.getId())
                .correlationId(entity.getCorrelationId())
                .messageId(entity.getMessageId())
                .userId(entity.getUserId())
                .category(CategoryCode.from(entity.getCategory().getCode()))
                .channel(NotificationChannelCode.from(entity.getChannel().getCode()))
                .recipientName(entity.getRecipientName())
                .recipientEmail(entity.getRecipientEmail())
                .recipientPhoneNumber(entity.getRecipientPhoneNumber())
                .status(NotificationStatus.valueOf(entity.getStatus()))
                .errorMessage(entity.getErrorMessage())
                .attemptCount(entity.getAttemptCount())
                .sentAt(entity.getSentAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
