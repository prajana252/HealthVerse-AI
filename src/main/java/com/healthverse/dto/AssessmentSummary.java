package com.healthverse.dto;

import java.time.Instant;

public record AssessmentSummary(String id, Instant createdAt, String label, String overallIndicator,
                                Double bmi, int riskFactorCount, boolean demo) {
}
