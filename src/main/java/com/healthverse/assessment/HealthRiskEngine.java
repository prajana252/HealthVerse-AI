package com.healthverse.assessment;

import com.healthverse.dto.*;
import com.healthverse.service.BmiCalculatorService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class HealthRiskEngine {
    private static final String DISCLAIMER = "HEALTHVERSE AI is an educational screening and risk-awareness tool. "
            + "It does not diagnose disease or predict that you will develop any condition. Speak to a qualified healthcare professional.";

    private final BmiCalculatorService bmiService;
    private final List<RiskAssessor> assessors;

    public HealthRiskEngine(BmiCalculatorService bmiService, List<RiskAssessor> assessors) {
        this.bmiService = bmiService;
        this.assessors = assessors;
    }

    public AssessmentResult analyze(AssessmentRequest r) {
        BmiResponse bmi = bmiService.calculate(new BmiRequest(r.heightCm(), r.weightKg()));
        List<RiskResult> risks = assessors.stream().map(a -> a.assess(r, bmi.bmi())).toList();
        long high = risks.stream().filter(x -> "HIGH".equals(x.level())).count();
        long moderate = risks.stream().filter(x -> "MODERATE".equals(x.level())).count();
        String overall = high >= 2 ? "HIGH" : (high == 1 || moderate >= 2) ? "MODERATE" : "LOW";
        Set<String> factors = new LinkedHashSet<>();
        Set<String> missing = new LinkedHashSet<>();
        risks.forEach(x -> { factors.addAll(x.factors()); missing.addAll(x.missingInputs()); });
        boolean emergency = r.symptoms().contains("chest_pain") || r.symptoms().contains("severe_breathlessness")
                || r.symptoms().contains("fainting");
        String warning = emergency
                ? "You reported symptoms that can be serious (chest pain, severe breathing difficulty or fainting). Seek medical attention immediately or call your local emergency number. Do not wait for this report."
                : null;
        return new AssessmentResult(bmi, overall, factors.size(), risks, warning, List.copyOf(missing), DISCLAIMER);
    }
}
