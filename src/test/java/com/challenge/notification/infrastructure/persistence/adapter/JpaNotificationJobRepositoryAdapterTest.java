package com.challenge.notification.infrastructure.persistence.adapter;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationJob;
import com.challenge.notification.domain.model.NotificationJobStatus;
import com.challenge.notification.infrastructure.persistence.entity.CategoryEntity;
import com.challenge.notification.infrastructure.persistence.entity.NotificationJobEntity;
import com.challenge.notification.infrastructure.persistence.mapper.NotificationJobPersistenceMapper;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataCategoryRepository;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataNotificationJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaNotificationJobRepositoryAdapterTest {

    @Mock
    private SpringDataNotificationJobRepository notificationJobRepository;

    @Mock
    private SpringDataCategoryRepository categoryRepository;

    private JpaNotificationJobRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaNotificationJobRepositoryAdapter(
                notificationJobRepository,
                categoryRepository,
                new NotificationJobPersistenceMapper()
        );
    }

    @Test
    void shouldSaveJob() {
        // given
        NotificationJob job = NotificationJob.initializePending(
                100L,
                "job-adapter-correlation",
                CategoryCode.SPORTS
        );

        CategoryEntity category = categoryEntity();

        NotificationJobEntity savedEntity = notificationJobEntity(
                10L,
                "job-adapter-correlation",
                "PENDING"
        );

        when(categoryRepository.findByCodeAndActiveTrue("SPORTS"))
                .thenReturn(Optional.of(category));

        when(notificationJobRepository.save(any(NotificationJobEntity.class)))
                .thenReturn(savedEntity);

        when(notificationJobRepository.findByIdWithCategory(10L))
                .thenReturn(Optional.of(savedEntity));

        // when
        NotificationJob savedJob = adapter.save(job);

        // assert
        assertThat(savedJob.getId()).isEqualTo(10L);
        assertThat(savedJob.getCorrelationId()).isEqualTo("job-adapter-correlation");
        assertThat(savedJob.getStatus()).isEqualTo(NotificationJobStatus.PENDING);
    }


    @Test
    void shouldFindPendingJobsForProcessing() {
        // given
        NotificationJobEntity pendingEntity = notificationJobEntity(
                10L,
                "job-adapter-correlation",
                "PENDING"
        );

        when(notificationJobRepository.findPendingJobsForProcessing(20))
                .thenReturn(List.of(pendingEntity));

        when(notificationJobRepository.findByIdWithCategory(10L))
                .thenReturn(Optional.of(pendingEntity));

        // when
        List<NotificationJob> jobs = adapter.findPendingJobsForProcessing(20);

        // assert
        assertThat(jobs).hasSize(1);
        assertThat(jobs.get(0).getId()).isEqualTo(10L);
        assertThat(jobs.get(0).getStatus()).isEqualTo(NotificationJobStatus.PENDING);
        assertThat(jobs.get(0).getCategory()).isEqualTo(CategoryCode.SPORTS);
    }

    @Test
    void shouldFindJobById() {
        // given
        NotificationJobEntity entity = notificationJobEntity(
                10L,
                "job-adapter-correlation",
                "PROCESSED"
        );

        when(notificationJobRepository.findByIdWithCategory(10L))
                .thenReturn(Optional.of(entity));

        // when
        Optional<NotificationJob> job = adapter.findById(10L);

        // assert
        assertThat(job).isPresent();
        assertThat(job.orElseThrow().getId()).isEqualTo(10L);
        assertThat(job.orElseThrow().getStatus()).isEqualTo(NotificationJobStatus.PROCESSED);
    }

    private static NotificationJobEntity notificationJobEntity(
            Long id,
            String correlationId,
            String status
    ) {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime lockedAt = status.equals("PENDING") ? null : now;
        LocalDateTime processedAt = status.equals("PROCESSED") ? now : null;

        return new NotificationJobEntity(
                id,
                correlationId,
                100L,
                categoryEntity(),
                status,
                status.equals("PENDING") ? 0 : 1,
                lockedAt,
                processedAt,
                null,
                now,
                now
        );
    }

    private static CategoryEntity categoryEntity() {
        return new CategoryEntity(
                (short) 1,
                "SPORTS",
                "Sports",
                true
        );
    }
}
