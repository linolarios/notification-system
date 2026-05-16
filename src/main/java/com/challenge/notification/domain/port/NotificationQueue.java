package com.challenge.notification.domain.port;

import com.challenge.notification.domain.model.NotificationJob;

import java.util.Optional;

public interface NotificationQueue {

    void enqueue(NotificationJob job);

    Optional<NotificationJob> poll();

    int size();
}
