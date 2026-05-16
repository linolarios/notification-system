package com.challenge.notification.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_channel_preferences")
public class UserChannelPreferenceEntity {

    @EmbeddedId
    private UserChannelPreferenceId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("channelId")
    @JoinColumn(name = "channel_id", nullable = false)
    private NotificationChannelEntity channel;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected UserChannelPreferenceEntity() {
    }

    public UserChannelPreferenceEntity(
            UserEntity user,
            NotificationChannelEntity channel,
            LocalDateTime createdAt
    ) {
        this.user = user;
        this.channel = channel;
        this.createdAt = createdAt;
    }

    public UserChannelPreferenceId getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public NotificationChannelEntity getChannel() {
        return channel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
