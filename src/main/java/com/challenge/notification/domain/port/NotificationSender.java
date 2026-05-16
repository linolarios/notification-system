package com.challenge.notification.domain.port;

import com.challenge.notification.domain.model.NotificationChannelCode;
import com.challenge.notification.domain.model.NotificationMessage;
import com.challenge.notification.domain.model.NotificationSendResult;
import com.challenge.notification.domain.model.NotificationSubscriber;

public interface NotificationSender {

    NotificationChannelCode getSupportedChannel();

    NotificationSendResult send(NotificationMessage message, NotificationSubscriber subscriber);
}
