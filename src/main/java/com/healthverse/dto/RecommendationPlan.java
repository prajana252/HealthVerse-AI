package com.healthverse.dto;

import java.util.List;

public record RecommendationPlan(List<Recommendation> recommendations, List<String> dailyGoals, List<String> weeklyGoals) {
}
