ALTER TABLE messages
    ADD COLUMN correlation_id VARCHAR(80);

ALTER TABLE notification_jobs
    ADD COLUMN correlation_id VARCHAR(80);

ALTER TABLE notification_logs
    ADD COLUMN correlation_id VARCHAR(80);

UPDATE messages
SET correlation_id = 'legacy-message-' || id
WHERE correlation_id IS NULL;

UPDATE notification_jobs
SET correlation_id = 'legacy-job-' || id
WHERE correlation_id IS NULL;

UPDATE notification_logs
SET correlation_id = 'legacy-log-' || id
WHERE correlation_id IS NULL;

ALTER TABLE messages
    ALTER COLUMN correlation_id SET NOT NULL;

ALTER TABLE notification_jobs
    ALTER COLUMN correlation_id SET NOT NULL;

ALTER TABLE notification_logs
    ALTER COLUMN correlation_id SET NOT NULL;

CREATE UNIQUE INDEX uk_messages_correlation_id
    ON messages (correlation_id);

CREATE INDEX idx_notification_jobs_correlation_id
    ON notification_jobs (correlation_id);

CREATE INDEX idx_notification_logs_correlation_id
    ON notification_logs (correlation_id);