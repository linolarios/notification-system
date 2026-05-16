package com.challenge.notification.infrastructure.queue;

import com.challenge.notification.domain.exception.NotificationQueueException;
import com.challenge.notification.domain.model.NotificationJob;
import com.challenge.notification.domain.port.NotificationQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Component
public class BlockingQueueNotificationQueue implements NotificationQueue {

    private final BlockingQueue<NotificationJob> queue;

    public BlockingQueueNotificationQueue(
            @Value("${notification.queue.capacity:1000}")
            int capacity
    ) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    @Override
    public void enqueue(NotificationJob job) {
        boolean accepted = queue.offer(job);

        if (!accepted) {
            throw new NotificationQueueException("Notification queue is full");
        }
    }

    @Override
    public Optional<NotificationJob> poll() {
        return Optional.ofNullable(queue.poll());
    }

    @Override
    public int size() {
        return queue.size();
    }
}
