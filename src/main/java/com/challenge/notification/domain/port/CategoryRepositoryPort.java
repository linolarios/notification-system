package com.challenge.notification.domain.port;

import com.challenge.notification.domain.model.CategoryCode;

import java.util.Optional;

public interface CategoryRepositoryPort {
    boolean existsActiveByCode(CategoryCode categoryCode);
}
