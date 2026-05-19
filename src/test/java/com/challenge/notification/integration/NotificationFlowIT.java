package com.challenge.notification.integration;

import com.challenge.notification.domain.service.NotificationJobProcessor;
import com.challenge.notification.infrastructure.persistence.entity.NotificationJobEntity;
import com.challenge.notification.infrastructure.persistence.entity.NotificationLogEntity;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataNotificationJobRepository;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataNotificationLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationFlowIT {

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
    private MockMvc mockMvc;

    @Autowired
    private NotificationJobProcessor notificationJobProcessor;

    @Autowired
    private SpringDataNotificationJobRepository notificationJobRepository;

    @Autowired
    private SpringDataNotificationLogRepository notificationLogRepository;

    @Test
    void shouldAcceptNotificationProcessJobAndCreateLogs() throws Exception {
        // given
        String correlationId = "integration-flow-correlation-001";

        String requestBody = """
                {
                  "category": "SPORTS",
                  "message": "Integration test notification"
                }
                """;

        // when
        mockMvc.perform(post("/api/notifications")
                        .header("X-Correlation-Id", correlationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.correlationId").value(correlationId))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        int processedCount = notificationJobProcessor.processPendingJobs(20);

        // assert
        assertThat(processedCount).isEqualTo(1);

        NotificationJobEntity job = notificationJobRepository.findByCorrelationId(correlationId)
                .orElseThrow();

        assertThat(job.getCorrelationId()).isEqualTo(correlationId);
        assertThat(job.getStatus()).isEqualTo("PROCESSED");
        assertThat(job.getAttemptCount()).isEqualTo(1);
        assertThat(job.getProcessedAt()).isNotNull();

        List<NotificationLogEntity> logs = notificationLogRepository
                .findByCorrelationIdOrderByCreatedAtDesc(
                        correlationId,
                        PageRequest.of(0, 20)
                )
                .getContent();

        assertThat(logs).isNotEmpty();

        assertThat(logs)
                .allSatisfy(log -> {
                    assertThat(log.getCorrelationId()).isEqualTo(correlationId);
                    assertThat(log.getMessageId()).isEqualTo(job.getMessageId());
                    assertThat(log.getStatus()).isIn("SENT", "FAILED");
                    assertThat(log.getAttemptCount()).isGreaterThanOrEqualTo(1);
                });
    }
}
