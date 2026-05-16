package com.challenge.notification.domain.service;

import com.challenge.notification.domain.model.NotificationChannelCode;
import com.challenge.notification.domain.model.NotificationDeliveryAttempt;
import com.challenge.notification.domain.model.NotificationMessage;
import com.challenge.notification.domain.model.NotificationSendResult;
import com.challenge.notification.domain.model.NotificationSubscriber;
import org.springframework.stereotype.Component;

@Component
public class NotificationDeliveryAttemptFactory {

    public NotificationDeliveryAttempt sent(
            NotificationMessage message,
            NotificationSubscriber subscriber,
            NotificationChannelCode channel,
            NotificationSendResult sendResult
    ) {
        return NotificationDeliveryAttempt.sent(
                message,
                subscriber,
                channel,
                sendResult
        );
    }

    public NotificationDeliveryAttempt failed(
            NotificationMessage message,
            NotificationSubscriber subscriber,
            NotificationChannelCode channel,
            String errorMessage
    ) {
        return NotificationDeliveryAttempt.failed(
                message,
                subscriber,
                channel,
                errorMessage
        );
    }
}
