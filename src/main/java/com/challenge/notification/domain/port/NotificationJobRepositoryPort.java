package com.challenge.notification.domain.port;

import com.challenge.notification.domain.model.NotificationJob;

import java.util.List;
import java.util.Optional;

public interface NotificationJobRepositoryPort {

    NotificationJob save(NotificationJob job);

    Optional<NotificationJob> findById(Long id);

    List<NotificationJob> findPendingJobsForProcessing(int limit);

    List<NotificationJob> findStaleProcessingJobs(int timeoutMinutes, int limit);
}
