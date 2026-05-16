package com.challenge.notification.infrastructure.persistence.repository;

import com.challenge.notification.infrastructure.persistence.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataMessageRepository extends JpaRepository<MessageEntity, Long> {
}
