package com.challenge.notification.application;

import com.challenge.notification.dto.response.*;
import com.challenge.notification.dto.response.mapper.NotificationLogResponseMapper;
import com.challenge.notification.infrastructure.cache.CategoryCacheService;
import com.challenge.notification.infrastructure.cache.NotificationChannelCacheService;
import com.challenge.notification.infrastructure.persistence.entity.CategoryEntity;
import com.challenge.notification.infrastructure.persistence.entity.NotificationChannelEntity;
import com.challenge.notification.infrastructure.persistence.entity.NotificationJobEntity;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataNotificationJobRepository;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataNotificationLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationQueryServiceImpl implements NotificationQueryService {

    private final SpringDataNotificationLogRepository notificationLogRepository;
    private final SpringDataNotificationJobRepository notificationJobRepository;
    private final CategoryCacheService categoryCacheService;
    private final NotificationChannelCacheService notificationChannelCacheService;
    private final NotificationLogResponseMapper logResponseMapper;


    public NotificationQueryServiceImpl(
            SpringDataNotificationLogRepository notificationLogRepository,
            SpringDataNotificationJobRepository notificationJobRepository,
            CategoryCacheService categoryCacheService,
            NotificationChannelCacheService notificationChannelCacheService,
            NotificationLogResponseMapper logResponseMapper
    ) {
        this.notificationLogRepository = notificationLogRepository;
        this.notificationJobRepository = notificationJobRepository;
        this.categoryCacheService = categoryCacheService;
        this.notificationChannelCacheService = notificationChannelCacheService;
        this.logResponseMapper = logResponseMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationLogResponse> getNotificationLogs(Pageable pageable) {
        Page<NotificationLogResponse> logResponsePage = notificationLogRepository
                .findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toNotificationLogResponse);

        return PagedResponse.fromPage(logResponsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationLogResponse> getNotificationLogsByCorrelationId(
            String correlationId,
            Pageable pageable
    ) {
        Page<NotificationLogResponse> logResponsePage = notificationLogRepository
                .findByCorrelationIdOrderByCreatedAtDesc(correlationId, pageable)
                .map(this::toNotificationLogResponse);

        return PagedResponse.fromPage(logResponsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationJobResponse getNotificationJob(Long jobId) {
        return notificationJobRepository.findById(jobId)
                .map(this::toNotificationJobResponse)
                .orElseThrow(() -> new IllegalArgumentException("Notification job not found: " + jobId));
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationJobResponse getNotificationJobByCorrelationId(String correlationId) {
        return notificationJobRepository.findByCorrelationId(correlationId)
                .map(this::toNotificationJobResponse)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Notification job not found for correlationId: " + correlationId
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        return categoryCacheService.getActiveCategories()
                .stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationChannelResponse> getActiveNotificationChannels() {
        return notificationChannelCacheService.getActiveNotificationChannels()
                .stream()
                .map(this::toNotificationChannelResponse)
                .toList();
    }

    private NotificationLogResponse toNotificationLogResponse(
            com.challenge.notification.infrastructure.persistence.entity.NotificationLogEntity entity
    ) {
        return logResponseMapper.toNotificationLogResponse(entity);
    }

    private NotificationJobResponse toNotificationJobResponse(NotificationJobEntity entity) {
        return new NotificationJobResponse(
                entity.getCorrelationId(),
                entity.getId(),
                entity.getMessageId(),
                entity.getStatus(),
                entity.getAttemptCount(),
                entity.getLastError(),
                entity.getCreatedAt(),
                entity.getProcessedAt()
        );
    }

    private CategoryResponse toCategoryResponse(CategoryEntity entity) {
        return new CategoryResponse(
                entity.getCode(),
                entity.getName()
        );
    }

    private NotificationChannelResponse toNotificationChannelResponse(NotificationChannelEntity entity) {
        return new NotificationChannelResponse(
                entity.getCode(),
                entity.getName()
        );
    }
}
