package com.challenge.notification.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserCategorySubscriptionId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "category_id")
    private Short categoryId;

    protected UserCategorySubscriptionId() {
    }

    public UserCategorySubscriptionId(Long userId, Short categoryId) {
        this.userId = userId;
        this.categoryId = categoryId;
    }

    public Long getUserId() {
        return userId;
    }

    public Short getCategoryId() {
        return categoryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserCategorySubscriptionId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId)
                && Objects.equals(categoryId, that.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, categoryId);
    }
}
