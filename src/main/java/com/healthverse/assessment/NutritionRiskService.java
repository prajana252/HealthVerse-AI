package com.healthverse.assessment;

import com.healthverse.dto.AssessmentRequest;
import com.healthverse.dto.RiskResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NutritionRiskService implements RiskAssessor {
    @Override
    public RiskResult assess(AssessmentRequest r, double bmi) {
        ScoreCard s = new ScoreCard();
        List<String> missing = new ArrayList<>();
        if (r.diet() == null) missing.add("Dietary habits");
        else s.add(ScoreCard.eq(r.diet(), "BALANCED") ? 0 : 2, "Diet pattern: " + r.diet().toLowerCase().replace('_', ' '));
        if (r.waterLitres() == null) missing.add("Daily water intake");
        else s.add(r.waterLitres() < 1.0 ? 2 : r.waterLitres() < 1.5 ? 1 : 0, "Water intake " + r.waterLitres() + " L/day");
        s.add(bmi < 18.5 ? 2 : bmi >= 30 ? 2 : bmi >= 25 ? 1 : 0, "BMI " + String.format("%.1f", bmi) + " outside the healthy range");
        String level = s.level(1, 3);
        return new RiskResult("Nutritional health indicators", level, s.points, "Rule-based indicator score", s.factors,
                "Score " + s.points + ". Based only on self-reported diet, water intake and BMI.",
                ScoreCard.steps(level, "Keep eating a varied diet with vegetables, fruit, pulses and whole grains.",
                        "Increase vegetables and whole foods, cut sugary and highly processed foods, and drink more water.",
                        "Consider seeing a registered dietitian for a personalised eating plan."), missing);
    }
}
