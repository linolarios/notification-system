package com.challenge.notification.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserChannelPreferenceId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "channel_id")
    private Short channelId;

    protected UserChannelPreferenceId() {
    }

    public UserChannelPreferenceId(Long userId, Short channelId) {
        this.userId = userId;
        this.channelId = channelId;
    }

    public Long getUserId() {
        return userId;
    }

    public Short getChannelId() {
        return channelId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserChannelPreferenceId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId)
                && Objects.equals(channelId, that.channelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, channelId);
    }
}
