package com.challenge.notification.infrastructure.persistence.repository;

import com.challenge.notification.infrastructure.persistence.entity.UserChannelPreferenceEntity;
import com.challenge.notification.infrastructure.persistence.entity.UserChannelPreferenceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataUserChannelPreferenceRepository
        extends JpaRepository<UserChannelPreferenceEntity, UserChannelPreferenceId> {

    List<UserChannelPreferenceEntity> findByIdUserId(Long userId);

    @Query("SELECT ucp.channel.code FROM UserChannelPreferenceEntity ucp " +
            "WHERE ucp.user.id = :userId")
    List<String> findChannelCodesByUserId(@Param("userId") Long userId);
}
