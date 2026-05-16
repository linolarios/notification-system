package com.challenge.notification.domain.port;

import com.challenge.notification.domain.model.NotificationMessage;

import java.util.Optional;

public interface MessageRepositoryPort {

    NotificationMessage save(NotificationMessage message);

    Optional<NotificationMessage> findById(Long id);
}
