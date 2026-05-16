package com.challenge.notification.domain.port;

import com.challenge.notification.domain.model.NotificationChannelCode;

public interface NotificationChannelRepositoryPort {

    boolean existsActiveByCode(NotificationChannelCode channelCode);
}
