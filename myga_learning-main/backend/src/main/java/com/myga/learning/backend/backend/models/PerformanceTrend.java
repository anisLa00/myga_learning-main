package com.myga.learning.backend.backend.models;

/** Direction of a student's results over time, derived from stored grades. */
public enum PerformanceTrend {
    IMPROVING,
    STABLE,
    DECLINING,
    /** Not enough grades recorded to say anything meaningful. */
    INSUFFICIENT_DATA
}
