package com.challenge.notification.infrastructure.queue;

import com.challenge.notification.domain.service.NotificationJobProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationDatabaseJobWorkerTest {

    @Mock
    private NotificationJobProcessor notificationJobProcessor;

    private NotificationDatabaseJobWorker worker;

    @BeforeEach
    void setUp() {
        worker = new NotificationDatabaseJobWorker(
                notificationJobProcessor,
                20
        );
    }

    @Test
    void shouldDelegatePendingJobProcessingToProcessor() {
        // given
        when(notificationJobProcessor.processPendingJobs(20))
                .thenReturn(3);

        // when
        worker.processPendingJobs();

        // assert
        verify(notificationJobProcessor).processPendingJobs(20);
    }

    @Test
    void shouldSwallowUnexpectedProcessorExceptionSoWorkerDoesNotCrash() {
        // given
        when(notificationJobProcessor.processPendingJobs(20))
                .thenThrow(new RuntimeException("Unexpected processor failure"));

        // when
        worker.processPendingJobs();

        // assert
        verify(notificationJobProcessor).processPendingJobs(20);
    }
}
