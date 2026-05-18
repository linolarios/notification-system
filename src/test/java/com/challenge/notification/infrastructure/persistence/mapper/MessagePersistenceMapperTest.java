package com.challenge.notification.infrastructure.persistence.mapper;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationMessage;
import com.challenge.notification.infrastructure.persistence.entity.CategoryEntity;
import com.challenge.notification.infrastructure.persistence.entity.MessageEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MessagePersistenceMapperTest {

    private final MessagePersistenceMapper mapper = new MessagePersistenceMapper();

    @Test
    void shouldMapDomainToEntity() {
        // given
        CategoryEntity categoryEntity = new CategoryEntity(
                (short) 1,
                "SPORTS",
                "Sports",
                true
        );

        NotificationMessage message = new NotificationMessage(
                100L,
                "mapper-correlation",
                CategoryCode.SPORTS,
                "Test body",
                LocalDateTime.now()
        );

        // when
        MessageEntity entity = mapper.toEntity(message, categoryEntity);

        // assert
        assertThat(entity.getId()).isEqualTo(100L);
        assertThat(entity.getCorrelationId()).isEqualTo("mapper-correlation");
        assertThat(entity.getCategory()).isSameAs(categoryEntity);
        assertThat(entity.getBody()).isEqualTo("Test body");
        assertThat(entity.getCreatedAt()).isEqualTo(message.getCreatedAt());
    }

    @Test
    void shouldMapEntityToDomain() {
        // given
        CategoryEntity categoryEntity = new CategoryEntity(
                (short) 1,
                "SPORTS",
                "Sports",
                true
        );

        MessageEntity entity = new MessageEntity(
                100L,
                "mapper-correlation",
                categoryEntity,
                "Test body",
                LocalDateTime.now()
        );

        // when
        NotificationMessage message = mapper.toDomain(entity);

        // assert
        assertThat(message.getId()).isEqualTo(100L);
        assertThat(message.getCorrelationId()).isEqualTo("mapper-correlation");
        assertThat(message.getCategory()).isEqualTo(CategoryCode.SPORTS);
        assertThat(message.getBody()).isEqualTo("Test body");
        assertThat(message.getCreatedAt()).isEqualTo(entity.getCreatedAt());
    }
}
