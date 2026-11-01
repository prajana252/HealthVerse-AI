package com.healthverse.assessment;

import com.healthverse.dto.AssessmentRequest;
import com.healthverse.dto.RiskResult;

public interface RiskAssessor {
    RiskResult assess(AssessmentRequest r, double bmi);
}
