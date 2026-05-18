package com.challenge.notification.infrastructure.queue;

import com.challenge.notification.domain.service.NotificationJobRecoveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationJobRecoveryWorkerTest {

    @Mock
    private NotificationJobRecoveryService recoveryService;

    private NotificationJobRecoveryWorker worker;

    @BeforeEach
    void setUp() {
        worker = new NotificationJobRecoveryWorker(
                recoveryService,
                5,
                20
        );
    }

    @Test
    void shouldDelegateRecoveryToService() {
        // given
        when(recoveryService.recoverStaleProcessingJobs(5, 20))
                .thenReturn(2);

        // when
        worker.recoverStaleJobs();

        // assert
        verify(recoveryService).recoverStaleProcessingJobs(5, 20);
    }

    @Test
    void shouldSwallowUnexpectedRecoveryException() {
        // given
        when(recoveryService.recoverStaleProcessingJobs(5, 20))
                .thenThrow(new RuntimeException("Recovery failed"));

        // when
        worker.recoverStaleJobs();

        // assert
        verify(recoveryService).recoverStaleProcessingJobs(5, 20);
    }
}
