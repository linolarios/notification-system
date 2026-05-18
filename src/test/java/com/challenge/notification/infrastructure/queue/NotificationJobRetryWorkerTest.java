package com.challenge.notification.infrastructure.queue;

import com.challenge.notification.domain.service.NotificationJobRetryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationJobRetryWorkerTest {

    @Mock
    private NotificationJobRetryService retryService;

    private NotificationJobRetryWorker worker;

    @BeforeEach
    void setUp() {
        worker = new NotificationJobRetryWorker(
                retryService,
                3,
                20
        );
    }

    @Test
    void shouldDelegateRetryToService() {
        // given
        when(retryService.retryFailedJobs(3, 20))
                .thenReturn(2);

        // when
        worker.retryFailedJobs();

        // assert
        verify(retryService).retryFailedJobs(3, 20);
    }

    @Test
    void shouldSwallowUnexpectedRetryException() {
        // given
        when(retryService.retryFailedJobs(3, 20))
                .thenThrow(new RuntimeException("Retry failed"));

        // when
        worker.retryFailedJobs();

        // assert
        verify(retryService).retryFailedJobs(3, 20);
    }
}
