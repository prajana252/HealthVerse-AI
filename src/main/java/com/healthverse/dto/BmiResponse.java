package com.healthverse.dto;

public record BmiResponse(
        double bmi,
        String category,
        String explanation,
        double healthyBmiMin,
        double healthyBmiMax,
        double healthyWeightMinKg,
        double healthyWeightMaxKg,
        String disclaimer) {
}
