package com.example.backend_toyproject.model.enums;

/**
 * Lifecycle phases a todo item can move through. Persisted as strings for clarity.
 */
public enum TodoStatus {
    CREATED,
    UPDATED,
    COMPLETED,
    DELETED,
    RESTORED
}
