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
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationJobRetryServiceTest {

    @Mock
    private NotificationJobRepositoryPort notificationJobRepositoryPort;

    private NotificationJobRetryService retryService;

    @BeforeEach
    void setUp() {
        retryService = new NotificationJobRetryService(notificationJobRepositoryPort);
    }

    @Test
    void shouldResetFailedJobsToPendingForRetry() {
        // given
        NotificationJob failedJob = failedJob();

        when(notificationJobRepositoryPort.findRetryableFailedJobs(3, 20))
                .thenReturn(List.of(failedJob));

        when(notificationJobRepositoryPort.save(any(NotificationJob.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        int retriedCount = retryService.retryFailedJobs(3, 20);

        // assert
        assertThat(retriedCount).isEqualTo(1);

        ArgumentCaptor<NotificationJob> captor =
                ArgumentCaptor.forClass(NotificationJob.class);

        verify(notificationJobRepositoryPort).save(captor.capture());

        NotificationJob retriedJob = captor.getValue();

        assertThat(retriedJob.getStatus()).isEqualTo(NotificationJobStatus.PENDING);
        assertThat(retriedJob.getLockedAt()).isNull();
        assertThat(retriedJob.getProcessedAt()).isNull();
        assertThat(retriedJob.getLastError()).isNull();
    }

    private static NotificationJob failedJob() {
        LocalDateTime now = LocalDateTime.now();

        return NotificationJob.builder()
                .id(10L)
                .correlationId("retry-test-correlation")
                .messageId(100L)
                .category(CategoryCode.SPORTS)
                .status(NotificationJobStatus.FAILED)
                .attemptCount(1)
                .lockedAt(now.minusMinutes(1))
                .processedAt(now)
                .lastError("Provider unavailable")
                .createdAt(now.minusMinutes(5))
                .updatedAt(now)
                .build();
    }
}
