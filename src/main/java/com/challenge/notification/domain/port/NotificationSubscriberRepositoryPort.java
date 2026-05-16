package com.challenge.notification.domain.port;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationSubscriber;

import java.util.List;
import java.util.Optional;

public interface NotificationSubscriberRepositoryPort {
    List<NotificationSubscriber> findActiveSubscribersByCategory(CategoryCode category);
}
