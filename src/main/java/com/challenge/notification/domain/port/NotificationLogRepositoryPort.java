package com.challenge.notification.domain.port;

import com.challenge.notification.domain.model.NotificationDeliveryAttempt;

import java.util.List;

public interface NotificationLogRepositoryPort {

    NotificationDeliveryAttempt save(NotificationDeliveryAttempt deliveryAttempt);

}
