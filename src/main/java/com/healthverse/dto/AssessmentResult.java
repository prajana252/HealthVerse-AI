package com.healthverse.dto;

import java.util.List;

public record AssessmentResult(BmiResponse bmi, String overallIndicator, int riskFactorCount,
                               List<RiskResult> risks, String emergencyWarning,
                               List<String> missingInputs, String disclaimer) {
}
