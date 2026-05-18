package com.challenge.notification.infrastructure.persistence.mapper;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationJob;
import com.challenge.notification.domain.model.NotificationJobStatus;
import com.challenge.notification.infrastructure.persistence.entity.CategoryEntity;
import com.challenge.notification.infrastructure.persistence.entity.NotificationJobEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationJobPersistenceMapperTest {

    private final NotificationJobPersistenceMapper mapper = new NotificationJobPersistenceMapper();

    @Test
    void shouldMapDomainToEntity() {
        // given
        CategoryEntity categoryEntity = categoryEntity();

        NotificationJob job = NotificationJob.builder()
                .id(10L)
                .correlationId("job-mapper-correlation")
                .messageId(100L)
                .category(CategoryCode.SPORTS)
                .status(NotificationJobStatus.PROCESSING)
                .attemptCount(1)
                .lockedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // when
        NotificationJobEntity entity = mapper.toEntity(job, categoryEntity);

        // assert
        assertThat(entity.getId()).isEqualTo(10L);
        assertThat(entity.getCorrelationId()).isEqualTo("job-mapper-correlation");
        assertThat(entity.getMessageId()).isEqualTo(100L);
        assertThat(entity.getCategory()).isSameAs(categoryEntity);
        assertThat(entity.getStatus()).isEqualTo("PROCESSING");
        assertThat(entity.getAttemptCount()).isEqualTo(1);
    }

    @Test
    void shouldMapEntityToDomain() {
        // given
        CategoryEntity categoryEntity = categoryEntity();

        NotificationJobEntity entity = new NotificationJobEntity(
                10L,
                "job-mapper-correlation",
                100L,
                categoryEntity,
                "PROCESSED",
                1,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // when
        NotificationJob job = mapper.toDomain(entity);

        // assert
        assertThat(job.getId()).isEqualTo(10L);
        assertThat(job.getCorrelationId()).isEqualTo("job-mapper-correlation");
        assertThat(job.getMessageId()).isEqualTo(100L);
        assertThat(job.getCategory()).isEqualTo(CategoryCode.SPORTS);
        assertThat(job.getStatus()).isEqualTo(NotificationJobStatus.PROCESSED);
        assertThat(job.getAttemptCount()).isEqualTo(1);
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
