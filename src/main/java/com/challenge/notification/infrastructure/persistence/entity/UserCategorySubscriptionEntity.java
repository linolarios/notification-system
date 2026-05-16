package com.challenge.notification.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_category_subscriptions")
public class UserCategorySubscriptionEntity {

    @EmbeddedId
    private UserCategorySubscriptionId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("categoryId")
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected UserCategorySubscriptionEntity() {
    }

    public UserCategorySubscriptionEntity(
            UserEntity user,
            CategoryEntity category,
            LocalDateTime createdAt
    ) {
        this.user = user;
        this.category = category;
        this.createdAt = createdAt;
    }

    public UserCategorySubscriptionId getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
