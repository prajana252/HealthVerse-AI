package com.healthverse.dto;

import jakarta.validation.constraints.*;
import java.util.Set;

/** All values collected in slides 1-4. Optional fields may be null; the engine reports them as missing. */
public record AssessmentRequest(
        @NotNull @Min(value = 18, message = "This tool supports adults (18+) only") @Max(120) Integer age,
        String sex,                       // male | female | undisclosed
        @NotNull @DecimalMin("50") @DecimalMax("272") Double heightCm,
        @NotNull @DecimalMin("10") @DecimalMax("500") Double weightKg,
        @DecimalMin("40") @DecimalMax("200") Double waistCm,
        @Min(70) @Max(250) Integer systolic,
        @Min(40) @Max(150) Integer diastolic,
        Set<String> conditions,           // diabetes, hypertension, asthma, copd, heart_disease
        String diabetesFamily,            // NONE | ONE_PARENT | BOTH_PARENTS
        Boolean heartFamily,
        String activityLevel,             // SEDENTARY | MODERATE | VIGOROUS
        @DecimalMin("0") @DecimalMax("24") Double sleepHours,
        @Min(1) @Max(5) Integer sleepQuality,
        String diet,                      // BALANCED | HIGH_PROCESSED | HIGH_SUGAR | LOW_VARIETY
        @DecimalMin("0") @DecimalMax("15") Double waterLitres,
        String tobacco,                   // NEVER | FORMER | CURRENT
        String alcohol,                   // NONE | OCCASIONAL | FREQUENT
        @Min(1) @Max(10) Integer stress,
        @DecimalMin("0") @DecimalMax("24") Double screenHours,
        Set<String> symptoms) {           // fatigue, chest_pain, breathlessness, cough, ...

    public AssessmentRequest {
        conditions = conditions == null ? Set.of() : conditions;
        symptoms = symptoms == null ? Set.of() : symptoms;
    }
}
