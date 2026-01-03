package com.example.backend_toyproject.model.enums;

/**
 * Priority levels exposed to clients. Stored as strings via JPA so DB rows stay readable.
 */
public enum Priority {
    URGENT,
    HIGH,
    NORMAL,
    LOW
}
