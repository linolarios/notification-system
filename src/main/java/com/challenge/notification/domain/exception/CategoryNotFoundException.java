package com.challenge.notification.domain.exception;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(String category) {
        super("Category not found or inactive: " + category);
    }
}
