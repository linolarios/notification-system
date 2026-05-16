package com.challenge.notification.infrastructure.persistence.mapper;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationMessage;
import com.challenge.notification.infrastructure.persistence.entity.CategoryEntity;
import com.challenge.notification.infrastructure.persistence.entity.MessageEntity;
import org.springframework.stereotype.Component;

@Component
public class MessagePersistenceMapper {

    public MessageEntity toEntity(NotificationMessage message, CategoryEntity categoryEntity) {
        return new MessageEntity(
                message.getId(),
                message.getCorrelationId(),
                categoryEntity,
                message.getBody(),
                message.getCreatedAt()
        );
    }

    public NotificationMessage toDomain(MessageEntity entity) {
        return new NotificationMessage(
                entity.getId(),
                entity.getCorrelationId(),
                CategoryCode.from(entity.getCategory().getCode()),
                entity.getBody(),
                entity.getCreatedAt()
        );
    }
}
