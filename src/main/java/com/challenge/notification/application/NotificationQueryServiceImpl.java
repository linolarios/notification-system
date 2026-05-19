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

/**
 * Application service responsible for notification read/query use cases.
 *
 * <p>This service assembles API response DTOs for notification logs, job status,
 * categories, and channels. Read-heavy catalog data is resolved through cache services,
 * while operational data, such as notification jobs and logs, is read directly from
 * persistence so clients receive the latest processing state.</p>
 */
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

    /**
     * Returns notification delivery logs for audit/history views.
     *
     * <p>Logs are ordered newest first by the repository so the UI can show the most recent
     * delivery activity at the top of the history table.</p>
     */
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

    /**
     * Searches notification logs using optional filters.
     *
     * <p>Correlation ID supports partial matching, while category uses exact catalog-code
     * matching. Pagination and sorting are delegated to the repository through the supplied
     * {@link Pageable}.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationLogResponse> searchNotificationLogs(String correlationId, String category, Pageable pageable) {
        String correlationIdPattern = toLikePattern(correlationId);
        String normalizedCategory = normalizeFilter(category);


        Page<NotificationLogResponse> logResponsePage = notificationLogRepository
                .searchLogs(correlationIdPattern, normalizedCategory, pageable)
                .map(this::toNotificationLogResponse);

        return PagedResponse.fromPage(logResponsePage);
    }

    /**
     * Returns the current processing state for a notification job.
     *
     * <p>The response is used by clients to track asynchronous processing after a
     * notification request has been accepted.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public NotificationJobResponse getNotificationJob(Long jobId) {
        return notificationJobRepository.findById(jobId)
                .map(this::toNotificationJobResponse)
                .orElseThrow(() -> new IllegalArgumentException("Notification job not found: " + jobId));
    }

    /**
     * Returns the notification job associated with a correlation ID.
     *
     * <p>This provides an alternate lookup path when clients track requests by correlation
     * ID instead of the generated job ID.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public NotificationJobResponse getNotificationJobByCorrelationId(String correlationId) {
        return notificationJobRepository.findByCorrelationId(correlationId)
                .map(this::toNotificationJobResponse)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Notification job not found for correlationId: " + correlationId
                ));
    }

    /**
     * Returns active notification categories for API clients and UI dropdowns.
     *
     * <p>Categories are read through a cache service because catalog data is read frequently
     * and changes rarely.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        return categoryCacheService.getActiveCategories()
                .stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    /**
     * Returns active notification channels for API clients.
     *
     * <p>Channels are read through a cache service because catalog data is read frequently
     * and changes rarely.</p>
     */
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

    private static String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.strip();
    }

    private static String toLikePattern(String value) {
        String normalizedValue = normalizeFilter(value);

        if (normalizedValue == null) {
            return null;
        }

        return "%" + normalizedValue.toLowerCase() + "%";
    }
}
