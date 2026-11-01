package com.healthverse.dto;

import java.time.Instant;

public record SavedAssessment(String id, Instant createdAt, String label, boolean demo,
                              AssessmentRequest request, AssessmentResult result, RecommendationPlan plan) {
}
