package com.challenge.notification.application;

import com.challenge.notification.config.CorrelationConstants;
import com.challenge.notification.domain.exception.CategoryNotFoundException;
import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationJob;
import com.challenge.notification.domain.model.NotificationMessage;
import com.challenge.notification.domain.port.CategoryRepositoryPort;
import com.challenge.notification.domain.port.MessageRepositoryPort;
import com.challenge.notification.domain.port.NotificationJobRepositoryPort;
import com.challenge.notification.domain.port.NotificationQueue;
import com.challenge.notification.dto.request.CreateNotificationRequest;
import com.challenge.notification.dto.response.NotificationAcceptedResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NotificationCommandServiceImpl implements NotificationCommandService {

    private final CategoryRepositoryPort categoryRepositoryPort;
    private final MessageRepositoryPort messageRepositoryPort;
    private final NotificationJobRepositoryPort notificationJobRepositoryPort;

    public NotificationCommandServiceImpl(
            CategoryRepositoryPort categoryRepositoryPort,
            MessageRepositoryPort messageRepositoryPort,
            NotificationJobRepositoryPort notificationJobRepositoryPort
    ) {
        this.categoryRepositoryPort = categoryRepositoryPort;
        this.messageRepositoryPort = messageRepositoryPort;
        this.notificationJobRepositoryPort = notificationJobRepositoryPort;
    }

    private static @NonNull NotificationMessage getNotificationMessage(CreateNotificationRequest request, CategoryCode categoryCode) {
        String correlationId = MDC.get(CorrelationConstants.CORRELATION_ID_MDC_KEY);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        return NotificationMessage.newMessage(
                correlationId,
                categoryCode,
                request.message()
        );
    }

    @Override
    @Transactional
    public NotificationAcceptedResponse createNotification(CreateNotificationRequest request) {
        CategoryCode categoryCode = getCategoryCode(request);

        NotificationMessage requestMessage = getNotificationMessage(request, categoryCode);

        NotificationMessage savedMessage = messageRepositoryPort.save(requestMessage);

        NotificationJob notificationJob = NotificationJob.initializePending(savedMessage.getId(), savedMessage.getCorrelationId(), savedMessage.getCategory());

        NotificationJob savedNotificationJob = notificationJobRepositoryPort.save(notificationJob);

        return NotificationAcceptedResponse.accepted(savedNotificationJob.getCorrelationId(), savedMessage.getId(), savedNotificationJob.getId());
    }

    private @NonNull CategoryCode getCategoryCode(CreateNotificationRequest request) {
        String category = request.category();
        CategoryCode categoryCode;
        try {
            categoryCode = CategoryCode.from(category);
        } catch (IllegalArgumentException e) {
            throw new CategoryNotFoundException(category);
        }

        if (!categoryRepositoryPort.existsActiveByCode(categoryCode)) {
            throw new CategoryNotFoundException(category);
        }
        return categoryCode;
    }
}
