package com.challenge.notification.infrastructure.persistence.mapper;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationJob;
import com.challenge.notification.domain.model.NotificationJobStatus;
import com.challenge.notification.infrastructure.persistence.entity.CategoryEntity;
import com.challenge.notification.infrastructure.persistence.entity.NotificationJobEntity;
import org.hibernate.annotations.Comment;
import org.springframework.stereotype.Component;

@Component
public class NotificationJobPersistenceMapper {

    public NotificationJobEntity toEntity(NotificationJob job, CategoryEntity categoryEntity) {
        return new NotificationJobEntity(
                job.getId(),
                job.getCorrelationId(),
                job.getMessageId(),
                categoryEntity,
                job.getStatus().name(),
                job.getAttemptCount(),
                job.getLockedAt(),
                job.getProcessedAt(),
                job.getLastError(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }

    public NotificationJob toDomain(NotificationJobEntity entity) {
        return NotificationJob.builder()
                .id(entity.getId())
                .correlationId(entity.getCorrelationId())
                .messageId(entity.getMessageId())
                .category(CategoryCode.from(entity.getCategory().getCode()))
                .status(NotificationJobStatus.valueOf(entity.getStatus()))
                .attemptCount(entity.getAttemptCount())
                .lockedAt(entity.getLockedAt())
                .processedAt(entity.getProcessedAt())
                .lastError(entity.getLastError())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
