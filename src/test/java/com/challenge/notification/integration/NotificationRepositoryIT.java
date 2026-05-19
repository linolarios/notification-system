package com.challenge.notification.integration;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationJob;
import com.challenge.notification.domain.model.NotificationJobStatus;
import com.challenge.notification.domain.model.NotificationMessage;
import com.challenge.notification.domain.port.MessageRepositoryPort;
import com.challenge.notification.domain.port.NotificationJobRepositoryPort;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataNotificationJobRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class NotificationRepositoryIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("notification_test")
            .withUsername("notification")
            .withPassword("notification");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired
    private MessageRepositoryPort messageRepositoryPort;

    @Autowired
    private NotificationJobRepositoryPort notificationJobRepositoryPort;

    @Autowired
    private SpringDataNotificationJobRepository springDataNotificationJobRepository;

    @Test
    @Transactional
    void shouldPersistAndLoadNotificationJobWithCategory() {
        // given
        NotificationMessage message = messageRepositoryPort.save(
                NotificationMessage.newMessage(
                        "repository-it-correlation",
                        CategoryCode.SPORTS,
                        "Repository integration test message"
                )
        );

        NotificationJob job = NotificationJob.initializePending(
                message.getId(),
                message.getCorrelationId(),
                message.getCategory()
        );

        // when
        NotificationJob savedJob = notificationJobRepositoryPort.save(job);
        Optional<NotificationJob> foundJob = notificationJobRepositoryPort.findById(savedJob.getId());

        // assert
        assertThat(savedJob.getId()).isNotNull();
        assertThat(foundJob).isPresent();
        assertThat(foundJob.orElseThrow().getCorrelationId()).isEqualTo("repository-it-correlation");
        assertThat(foundJob.orElseThrow().getMessageId()).isEqualTo(message.getId());
        assertThat(foundJob.orElseThrow().getCategory()).isEqualTo(CategoryCode.SPORTS);
        assertThat(foundJob.orElseThrow().getStatus()).isEqualTo(NotificationJobStatus.PENDING);
    }

    @Test
    @Transactional
    void shouldFindPendingJobsForProcessingUsingLockingQuery() {
        // given
        NotificationMessage message = messageRepositoryPort.save(
                NotificationMessage.newMessage(
                        "pending-job-it-correlation",
                        CategoryCode.SPORTS,
                        "Pending job integration test message"
                )
        );

        NotificationJob job = NotificationJob.initializePending(
                message.getId(),
                message.getCorrelationId(),
                message.getCategory()
        );

        NotificationJob savedJob = notificationJobRepositoryPort.save(job);

        // when
        List<NotificationJob> pendingJobs = notificationJobRepositoryPort.findPendingJobsForProcessing(20);

        // assert
        assertThat(pendingJobs)
                .extracting(NotificationJob::getId)
                .contains(savedJob.getId());
    }

    @Test
    @Transactional
    void shouldFetchJobsWithCategoryByIds() {
        // given
        NotificationMessage message = messageRepositoryPort.save(
                NotificationMessage.newMessage(
                        "fetch-by-ids-it-correlation",
                        CategoryCode.SPORTS,
                        "Fetch by IDs integration test message"
                )
        );

        NotificationJob job = NotificationJob.initializePending(
                message.getId(),
                message.getCorrelationId(),
                message.getCategory()
        );

        NotificationJob savedJob = notificationJobRepositoryPort.save(job);

        // when
        var entities = springDataNotificationJobRepository.findAllByIdInWithCategory(
                List.of(savedJob.getId())
        );

        // assert
        assertThat(entities).hasSize(1);
        assertThat(entities.get(0).getId()).isEqualTo(savedJob.getId());
        assertThat(entities.get(0).getCategory()).isNotNull();
        assertThat(entities.get(0).getCategory().getCode()).isEqualTo("SPORTS");
    }
}
