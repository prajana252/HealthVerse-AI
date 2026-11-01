package com.healthverse.dto;

import jakarta.validation.constraints.NotBlank;

public record HistoryEntry(@NotBlank String id, @NotBlank String accessCode) {
}
