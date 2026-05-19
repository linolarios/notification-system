package com.challenge.notification.infrastructure.persistence.adapter;

import com.challenge.notification.domain.model.NotificationJob;
import com.challenge.notification.domain.port.NotificationJobRepositoryPort;
import com.challenge.notification.infrastructure.persistence.entity.CategoryEntity;
import com.challenge.notification.infrastructure.persistence.entity.NotificationJobEntity;
import com.challenge.notification.infrastructure.persistence.mapper.NotificationJobPersistenceMapper;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataCategoryRepository;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataNotificationJobRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class JpaNotificationJobRepositoryAdapter implements NotificationJobRepositoryPort {

    private final SpringDataNotificationJobRepository notificationJobRepository;
    private final SpringDataCategoryRepository categoryRepository;
    private final NotificationJobPersistenceMapper notificationJobPersistenceMapper;

    public JpaNotificationJobRepositoryAdapter(
            SpringDataNotificationJobRepository notificationJobRepository,
            SpringDataCategoryRepository categoryRepository,
            NotificationJobPersistenceMapper notificationJobPersistenceMapper
    ) {
        this.notificationJobRepository = notificationJobRepository;
        this.categoryRepository = categoryRepository;
        this.notificationJobPersistenceMapper = notificationJobPersistenceMapper;
    }

    @Override
    public NotificationJob save(NotificationJob job) {
        CategoryEntity categoryEntity = categoryRepository.findByCodeAndActiveTrue(job.getCategory().name())
                .orElseThrow(() -> new IllegalArgumentException("Active category not found: " + job.getCategory()));

        NotificationJobEntity newEntity = notificationJobPersistenceMapper.toEntity(job, categoryEntity);
        NotificationJobEntity savedEntity = notificationJobRepository.save(newEntity);
        NotificationJobEntity savedEntityWithCategory = notificationJobRepository.findByIdWithCategory(savedEntity.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Notification job not found after save: " + savedEntity.getId()
                ));

        return notificationJobPersistenceMapper.toDomain(savedEntityWithCategory);
    }

    @Override
    public Optional<NotificationJob> findById(Long id) {
        return notificationJobRepository.findByIdWithCategory(id)
                .map(notificationJobPersistenceMapper::toDomain);
    }

    @Override
    public List<NotificationJob> findPendingJobsForProcessing(int limit) {
        List<Long> jobIds = notificationJobRepository.findPendingJobIdsForProcessing(limit);
        return findJobsByIdsWithCategory(jobIds);
    }

    @Override
    public List<NotificationJob> findStaleProcessingJobs(int timeoutMinutes, int limit) {
        List<Long> jobIds = notificationJobRepository.findStaleProcessingJobIds(timeoutMinutes, limit);
        return findJobsByIdsWithCategory(jobIds);
    }

    @Override
    public List<NotificationJob> findRetryableFailedJobs(int maxAttempts, int limit) {
        List<Long> jobIds = notificationJobRepository.findRetryableFailedJobIds(maxAttempts, limit);
        return findJobsByIdsWithCategory(jobIds);
    }

    /**
     * Loads full notification job entities after a worker query has selected their IDs.
     *
     * <p>The worker queries use native SQL for locking. This second query fetches the
     * complete JPA entities with category data initialized so they can be safely mapped
     * to domain objects.</p>
     */
    private List<NotificationJob> findJobsByIdsWithCategory(List<Long> jobIds) {
        if (jobIds.isEmpty()) {
            return List.of();
        }
        List<NotificationJobEntity> entities = notificationJobRepository.findAllByIdInWithCategory(jobIds);

        Map<Long, NotificationJobEntity> entitiesById = entities.stream()
                .collect(Collectors.toMap(NotificationJobEntity::getId, Function.identity()));

        List<NotificationJob> jobs = new ArrayList<>(entities.size());

        for (Long jobId : jobIds) {
            NotificationJobEntity entity = entitiesById.get(jobId);

            if (entity != null) {
                jobs.add(notificationJobPersistenceMapper.toDomain(entity));
            }
        }

        return jobs;
    }
}
