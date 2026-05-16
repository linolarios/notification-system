package com.challenge.notification.application;

import com.challenge.notification.dto.response.*;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface NotificationQueryService {

    PagedResponse<NotificationLogResponse> getNotificationLogs(Pageable pageable);

    PagedResponse<NotificationLogResponse> getNotificationLogsByCorrelationId(String correlationId, Pageable pageable);

    NotificationJobResponse getNotificationJob(Long jobId);

    NotificationJobResponse getNotificationJobByCorrelationId(String correlationId);

    List<CategoryResponse> getActiveCategories();

    List<NotificationChannelResponse> getActiveNotificationChannels();
}
