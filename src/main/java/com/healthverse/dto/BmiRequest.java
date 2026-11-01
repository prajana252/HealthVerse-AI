package com.healthverse.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record BmiRequest(
        @NotNull(message = "Height is required")
        @DecimalMin(value = "50.0", message = "Height must be at least 50 cm")
        @DecimalMax(value = "272.0", message = "Height must be at most 272 cm")
        Double heightCm,

        @NotNull(message = "Weight is required")
        @DecimalMin(value = "10.0", message = "Weight must be at least 10 kg")
        @DecimalMax(value = "500.0", message = "Weight must be at most 500 kg")
        Double weightKg) {
}
