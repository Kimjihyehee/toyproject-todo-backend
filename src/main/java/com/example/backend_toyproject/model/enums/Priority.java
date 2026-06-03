package com.example.backend_toyproject.model.enums;

/**
 * Priority levels exposed to clients. Stored as strings via JPA so DB rows stay readable.
 * 낮음 -> 보통 -> 높음 -> 최상
 */
public enum Priority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}
