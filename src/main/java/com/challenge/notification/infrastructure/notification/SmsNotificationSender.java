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
public class SmsNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationSender.class);

    @Override
    public NotificationChannelCode getSupportedChannel() {
        return NotificationChannelCode.SMS;
    }

    @Override
    public NotificationSendResult send(NotificationMessage message, NotificationSubscriber subscriber) {
        log.info(
                "Simulating SMS notification to phoneNumber={} for messageId={} category={}",
                subscriber.getPhoneNumber(),
                message.getId(),
                message.getCategory()
        );

        return NotificationSendResult.success("SMS ::" + message.getId() + "-" + subscriber.getId());
    }
}
