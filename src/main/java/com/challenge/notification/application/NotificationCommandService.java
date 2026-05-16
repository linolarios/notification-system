package com.challenge.notification.application;

import com.challenge.notification.dto.request.CreateNotificationRequest;
import com.challenge.notification.dto.response.NotificationAcceptedResponse;

public interface NotificationCommandService {
    NotificationAcceptedResponse createNotification(CreateNotificationRequest request);
}
