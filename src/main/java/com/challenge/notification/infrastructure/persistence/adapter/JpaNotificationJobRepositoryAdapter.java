package com.challenge.notification.infrastructure.persistence.adapter;

import com.challenge.notification.domain.model.NotificationJob;
import com.challenge.notification.domain.port.NotificationJobRepositoryPort;
import com.challenge.notification.infrastructure.persistence.entity.CategoryEntity;
import com.challenge.notification.infrastructure.persistence.entity.NotificationJobEntity;
import com.challenge.notification.infrastructure.persistence.mapper.NotificationJobPersistenceMapper;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataCategoryRepository;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataNotificationJobRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
        return fetchPendingNotificationJobs(limit)
                .flatMap(Optional::stream)
                .map(notificationJobPersistenceMapper::toDomain)
                .toList();
    }

    private @NonNull Stream<Optional<NotificationJobEntity>> fetchPendingNotificationJobs(int limit) {
        return notificationJobRepository.findPendingJobsForProcessing(limit)
                .stream()
                .map(NotificationJobEntity::getId)
                .map(notificationJobRepository::findByIdWithCategory);
    }
}
