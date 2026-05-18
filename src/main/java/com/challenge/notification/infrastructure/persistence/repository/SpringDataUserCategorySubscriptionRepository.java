package com.challenge.notification.infrastructure.persistence.repository;

import com.challenge.notification.infrastructure.persistence.entity.UserCategorySubscriptionEntity;
import com.challenge.notification.infrastructure.persistence.entity.UserCategorySubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataUserCategorySubscriptionRepository
        extends JpaRepository<UserCategorySubscriptionEntity, UserCategorySubscriptionId> {

    @Query("""
            SELECT subscription
            FROM UserCategorySubscriptionEntity subscription
            JOIN FETCH subscription.user
            JOIN FETCH subscription.category category
            WHERE category.code = :categoryCode
              AND category.active = true
            """)
    List<UserCategorySubscriptionEntity> findActiveSubscriptionsByCategoryCode(
            @Param("categoryCode") String categoryCode
    );

    List<UserCategorySubscriptionEntity> findByIdUserId(Long userId);
}
