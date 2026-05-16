package com.challenge.notification.infrastructure.persistence.adapter;

import com.challenge.notification.domain.model.NotificationMessage;
import com.challenge.notification.domain.port.MessageRepositoryPort;
import com.challenge.notification.infrastructure.persistence.entity.CategoryEntity;
import com.challenge.notification.infrastructure.persistence.entity.MessageEntity;
import com.challenge.notification.infrastructure.persistence.mapper.MessagePersistenceMapper;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataCategoryRepository;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataMessageRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaMessageRepositoryAdapter implements MessageRepositoryPort {

    private final SpringDataMessageRepository messageRepository;
    private final SpringDataCategoryRepository categoryRepository;
    private final MessagePersistenceMapper messagePersistenceMapper;

    public JpaMessageRepositoryAdapter(
            SpringDataMessageRepository messageRepository,
            SpringDataCategoryRepository categoryRepository,
            MessagePersistenceMapper messagePersistenceMapper
    ) {
        this.messageRepository = messageRepository;
        this.categoryRepository = categoryRepository;
        this.messagePersistenceMapper = messagePersistenceMapper;
    }

    @Override
    public NotificationMessage save(NotificationMessage message) {
        CategoryEntity categoryEntity = categoryRepository.findByCodeAndActiveTrue(message.getCategory().name())
                .orElseThrow(() -> new IllegalArgumentException("Active category not found: " + message.getCategory()));

        MessageEntity entity = messagePersistenceMapper.toEntity(message, categoryEntity);
        MessageEntity savedEntity = messageRepository.save(entity);

        return messagePersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<NotificationMessage> findById(Long id) {
        return messageRepository.findById(id)
                .map(messagePersistenceMapper::toDomain);
    }
}
