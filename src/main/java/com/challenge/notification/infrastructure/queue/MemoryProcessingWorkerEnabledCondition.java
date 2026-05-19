package com.challenge.notification.infrastructure.queue;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Enables memory-backed notification workers only when memory processing mode is active
 * and workers are explicitly enabled.
 *
 * <p>This composite condition keeps scheduled workers disabled in tests or environments
 * where background processing should not start automatically.</p>
 */
class MemoryProcessingWorkerEnabledCondition extends AllNestedConditions {

    MemoryProcessingWorkerEnabledCondition() {
        super(ConfigurationPhase.PARSE_CONFIGURATION);
    }

    @ConditionalOnProperty(
            prefix = "notification.queue",
            name = "processing-mode",
            havingValue = "memory"
    )
    static class MemoryProcessingModeEnabled {
    }

    @ConditionalOnProperty(
            prefix = "notification.worker",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    static class WorkerEnabled {
    }
}
