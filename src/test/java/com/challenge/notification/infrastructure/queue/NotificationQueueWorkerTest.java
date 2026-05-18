package com.challenge.notification.infrastructure.queue;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationJob;
import com.challenge.notification.domain.port.NotificationQueue;
import com.challenge.notification.domain.service.NotificationJobProcessor;
import com.challenge.notification.config.CorrelationConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationQueueWorkerTest {

    @Mock
    private NotificationQueue notificationQueue;

    @Mock
    private NotificationJobProcessor notificationJobProcessor;

    private NotificationQueueWorker worker;

    @BeforeEach
    void setUp() {
        worker = new NotificationQueueWorker(
                notificationQueue,
                notificationJobProcessor,
                2
        );
    }

    @Test
    void shouldProcessJobsUpToBatchSize() {
        // given
        NotificationJob firstJob = job(10L, "first-correlation");
        NotificationJob secondJob = job(20L, "second-correlation");

        when(notificationQueue.poll())
                .thenReturn(Optional.of(firstJob))
                .thenReturn(Optional.of(secondJob));

        // when
        worker.pollQueue();

        // assert
        verify(notificationJobProcessor).process(firstJob);
        verify(notificationJobProcessor).process(secondJob);
        verify(notificationQueue, times(2)).poll();
        assertThat(MDC.get(CorrelationConstants.CORRELATION_ID_MDC_KEY)).isNull();
    }

    @Test
    void shouldStopPollingWhenQueueIsEmpty() {
        // given
        NotificationJob job = job(10L, "job-correlation");

        when(notificationQueue.poll())
                .thenReturn(Optional.of(job))
                .thenReturn(Optional.empty());

        // when
        worker.pollQueue();

        // assert
        verify(notificationJobProcessor).process(job);
        verify(notificationQueue, times(2)).poll();
    }

    @Test
    void shouldNotProcessAnythingWhenQueueIsEmpty() {
        // given
        when(notificationQueue.poll())
                .thenReturn(Optional.empty());

        // when
        worker.pollQueue();

        // assert
        verify(notificationJobProcessor, never()).process(org.mockito.ArgumentMatchers.any(NotificationJob.class));
        assertThat(MDC.get(CorrelationConstants.CORRELATION_ID_MDC_KEY)).isNull();
    }

    @Test
    void shouldContinuePollingWhenProcessingFails() {
        // given
        NotificationJob firstJob = job(10L, "first-correlation");
        NotificationJob secondJob = job(20L, "second-correlation");

        when(notificationQueue.poll())
                .thenReturn(Optional.of(firstJob))
                .thenReturn(Optional.of(secondJob));

        doThrow(new RuntimeException("Unexpected processing failure"))
                .when(notificationJobProcessor)
                .process(firstJob);

        // when
        worker.pollQueue();

        // assert
        verify(notificationJobProcessor).process(firstJob);
        verify(notificationJobProcessor).process(secondJob);
        assertThat(MDC.get(CorrelationConstants.CORRELATION_ID_MDC_KEY)).isNull();
    }

    private static NotificationJob job(Long id, String correlationId) {
        return NotificationJob.initializePending(
                        100L,
                        correlationId,
                        CategoryCode.SPORTS
                )
                .toBuilder()
                .id(id)
                .build();
    }
}
