package com.challenge.notification.domain.service;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationJob;
import com.challenge.notification.domain.model.NotificationJobStatus;
import com.challenge.notification.domain.port.NotificationJobRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationJobRecoveryServiceTest {

    @Mock
    private NotificationJobRepositoryPort notificationJobRepositoryPort;

    private NotificationJobRecoveryService recoveryService;

    @BeforeEach
    void setUp() {
        recoveryService = new NotificationJobRecoveryService(notificationJobRepositoryPort);
    }

    @Test
    void shouldResetStaleProcessingJobsToPending() {
        // given
        NotificationJob staleJob = processingJob();

        when(notificationJobRepositoryPort.findStaleProcessingJobs(5, 20))
                .thenReturn(List.of(staleJob));

        when(notificationJobRepositoryPort.save(org.mockito.Mockito.any(NotificationJob.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        int recoveredCount = recoveryService.recoverStaleProcessingJobs(5, 20);

        // assert
        assertThat(recoveredCount).isEqualTo(1);

        ArgumentCaptor<NotificationJob> jobCaptor =
                ArgumentCaptor.forClass(NotificationJob.class);

        verify(notificationJobRepositoryPort).save(jobCaptor.capture());

        NotificationJob recoveredJob = jobCaptor.getValue();

        assertThat(recoveredJob.getStatus()).isEqualTo(NotificationJobStatus.PENDING);
        assertThat(recoveredJob.getLockedAt()).isNull();
        assertThat(recoveredJob.getProcessedAt()).isNull();
        assertThat(recoveredJob.getLastError()).isNull();
    }

    private static NotificationJob processingJob() {
        LocalDateTime now = LocalDateTime.now();

        return NotificationJob.builder()
                .id(10L)
                .correlationId("recovery-test-correlation")
                .messageId(100L)
                .category(CategoryCode.SPORTS)
                .status(NotificationJobStatus.PROCESSING)
                .attemptCount(1)
                .lockedAt(now.minusMinutes(10))
                .createdAt(now.minusMinutes(15))
                .updatedAt(now.minusMinutes(10))
                .build();
    }
}
