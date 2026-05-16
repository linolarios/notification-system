package com.challenge.notification.infrastructure.persistence.repository;

import com.challenge.notification.infrastructure.persistence.entity.UserCategorySubscriptionEntity;
import com.challenge.notification.infrastructure.persistence.entity.UserCategorySubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataUserCategorySubscriptionRepository
        extends JpaRepository<UserCategorySubscriptionEntity, UserCategorySubscriptionId> {

    @Query("SELECT ucs FROM UserCategorySubscriptionEntity ucs " +
            "JOIN FETCH ucs.user " +
            "JOIN FETCH ucs.category c " +
            "WHERE c.code = :categoryCode AND c.active = true")
    List<UserCategorySubscriptionEntity> findActiveSubscriptionsByCategoryCode(@Param("categoryCode") String categoryCode);

    List<UserCategorySubscriptionEntity> findByIdUserId(Long userId);

    @Query("SELECT ucs.user FROM UserCategorySubscriptionEntity ucs " +
            "WHERE ucs.category.code = :categoryCode")
    List<com.challenge.notification.infrastructure.persistence.entity.UserEntity>
    findUsersByCategoryCode(@Param("categoryCode") String categoryCode);
}
