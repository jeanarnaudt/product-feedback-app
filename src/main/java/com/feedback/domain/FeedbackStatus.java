package com.feedback.domain;

/**
 * Mirrors the MySQL ENUM in V1__init_schema.sql for feedback.status
 */
public enum FeedbackStatus {
    SUGGESTION,
    PLANNED,
    IN_PROGRESS,
    LIVE
}
