
package com.challenge.notification.infrastructure.persistence.adapter;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationMessage;
import com.challenge.notification.infrastructure.persistence.entity.CategoryEntity;
import com.challenge.notification.infrastructure.persistence.entity.MessageEntity;
import com.challenge.notification.infrastructure.persistence.mapper.MessagePersistenceMapper;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataCategoryRepository;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaMessageRepositoryAdapterTest {

    @Mock
    private SpringDataMessageRepository messageRepository;

    @Mock
    private SpringDataCategoryRepository categoryRepository;

    private JpaMessageRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaMessageRepositoryAdapter(
                messageRepository,
                categoryRepository,
                new MessagePersistenceMapper()
        );
    }

    @Test
    void shouldSaveMessage() {
        // given
        CategoryEntity category = categoryEntity();

        NotificationMessage message = new NotificationMessage(
                null,
                "message-adapter-correlation",
                CategoryCode.SPORTS,
                "Message body",
                LocalDateTime.now()
        );

        when(categoryRepository.findByCodeAndActiveTrue("SPORTS"))
                .thenReturn(Optional.of(category));

        when(messageRepository.save(any(MessageEntity.class)))
                .thenAnswer(invocation -> {
                    MessageEntity entity = invocation.getArgument(0);

                    return new MessageEntity(
                            100L,
                            entity.getCorrelationId(),
                            entity.getCategory(),
                            entity.getBody(),
                            entity.getCreatedAt()
                    );
                });

        // when
        NotificationMessage savedMessage = adapter.save(message);

        // assert
        assertThat(savedMessage.getId()).isEqualTo(100L);
        assertThat(savedMessage.getCorrelationId()).isEqualTo("message-adapter-correlation");
        assertThat(savedMessage.getCategory()).isEqualTo(CategoryCode.SPORTS);
        assertThat(savedMessage.getBody()).isEqualTo("Message body");
    }

    @Test
    void shouldThrowWhenActiveCategoryDoesNotExistOnSave() {
        // given
        NotificationMessage message = new NotificationMessage(
                null,
                "message-adapter-correlation",
                CategoryCode.SPORTS,
                "Message body",
                LocalDateTime.now()
        );

        when(categoryRepository.findByCodeAndActiveTrue("SPORTS"))
                .thenReturn(Optional.empty());

        // when / assert
        assertThatThrownBy(() -> adapter.save(message))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Active category not found");
    }

    @Test
    void shouldFindMessageById() {
        // given
        MessageEntity entity = new MessageEntity(
                100L,
                "message-adapter-correlation",
                categoryEntity(),
                "Message body",
                LocalDateTime.now()
        );

        when(messageRepository.findById(100L))
                .thenReturn(Optional.of(entity));

        // when
        Optional<NotificationMessage> message = adapter.findById(100L);

        // assert
        assertThat(message).isPresent();
        assertThat(message.orElseThrow().getId()).isEqualTo(100L);
        assertThat(message.orElseThrow().getCategory()).isEqualTo(CategoryCode.SPORTS);
    }

    private static CategoryEntity categoryEntity() {
        return new CategoryEntity(
                (short) 1,
                "SPORTS",
                "Sports",
                true
        );
    }
}