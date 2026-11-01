package com.healthverse.dto;

import java.util.List;

/** level: LOW | MODERATE | HIGH | NOT_APPLICABLE | INSUFFICIENT_DATA. score is null when no scoring method applies. */
public record RiskResult(String category, String level, Integer score, String method,
                         List<String> factors, String explanation,
                         List<String> nextSteps, List<String> missingInputs) {
}
