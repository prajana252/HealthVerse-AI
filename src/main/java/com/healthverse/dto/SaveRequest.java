package com.healthverse.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** No full name is accepted or stored: a short optional label (e.g. "March check") is enough. */
public record SaveRequest(@NotNull @Valid AssessmentRequest assessment, @Size(max = 60) String label, Boolean demo) {
}
