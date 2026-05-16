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
public class EmailNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);

    @Override
    public NotificationChannelCode getSupportedChannel() {
        return NotificationChannelCode.EMAIL;
    }

    @Override
    public NotificationSendResult send(NotificationMessage message, NotificationSubscriber subscriber) {
        log.info(
                "Simulating EMAIL notification to email={} for messageId={} category={}",
                subscriber.getEmail(),
                message.getId(),
                message.getCategory()
        );

        return NotificationSendResult.success("e-mail :: " + message.getId() + " :: " + subscriber.getId());
    }
}
