package com.challenge.notification.domain.service;

import com.challenge.notification.domain.model.NotificationJob;
import com.challenge.notification.domain.port.NotificationJobRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationJobRecoveryService {

    private static final Logger log = LoggerFactory.getLogger(NotificationJobRecoveryService.class);

    private final NotificationJobRepositoryPort notificationJobRepositoryPort;

    public NotificationJobRecoveryService(NotificationJobRepositoryPort notificationJobRepositoryPort) {
        this.notificationJobRepositoryPort = notificationJobRepositoryPort;
    }

    /**
     * Resets jobs that have been in 'processing' state for longer than the specified timeout.
     *
     * @param timeoutMinutes
     * @param batchSize
     * @return
     */
    @Transactional
    public int recoverStaleProcessingJobs(int timeoutMinutes, int batchSize) {
        List<NotificationJob> staleJobs =
                notificationJobRepositoryPort.findStaleProcessingJobs(timeoutMinutes, batchSize);

        for (NotificationJob staleJob : staleJobs) {
            NotificationJob pendingJob = staleJob.resetToPendingForRetry();
            notificationJobRepositoryPort.save(pendingJob);

            log.warn(
                    "Recovered stale notification job. jobId={} correlationId={}",
                    staleJob.getId(),
                    staleJob.getCorrelationId()
            );
        }

        return staleJobs.size();
    }
}
