package com.challenge.notification.infrastructure.notification;

import com.challenge.notification.domain.model.NotificationChannelCode;
import com.challenge.notification.domain.model.NotificationMessage;
import com.challenge.notification.domain.model.NotificationSendResult;
import com.challenge.notification.domain.model.NotificationSubscriber;
import com.challenge.notification.domain.port.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PushNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationSender.class);

    @Override
    public NotificationChannelCode getSupportedChannel() {
        return NotificationChannelCode.PUSH;
    }

    @Override
    public NotificationSendResult send(NotificationMessage message, NotificationSubscriber subscriber) {
        log.info(
                "Simulating PUSH notification to userId={} for messageId={} category={}",
                subscriber.getId(),
                message.getId(),
                message.getCategory()
        );

        return NotificationSendResult.success("PUSH :: " + message.getId() + " :: " + subscriber.getId());
    }
}
