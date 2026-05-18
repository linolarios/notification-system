package com.challenge.notification.infrastructure.persistence.repository;

import com.challenge.notification.infrastructure.persistence.entity.UserChannelPreferenceEntity;
import com.challenge.notification.infrastructure.persistence.entity.UserChannelPreferenceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataUserChannelPreferenceRepository extends JpaRepository<UserChannelPreferenceEntity, UserChannelPreferenceId> {

    List<UserChannelPreferenceEntity> findByIdUserId(Long userId);

}
