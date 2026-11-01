package com.healthverse.assessment;

import com.healthverse.dto.AssessmentRequest;
import com.healthverse.dto.RiskResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses the Indian Diabetes Risk Score (IDRS, Mohan et al., Madras Diabetes Research Foundation)
 * when age, sex, waist and activity are available. Otherwise falls back to a simple rule-based indicator.
 * IDRS bands: below 30 low, 30-50 moderate, 60 and above high.
 */
@Service
public class DiabetesRiskService implements RiskAssessor {
    @Override
    public RiskResult assess(AssessmentRequest r, double bmi) {
        String cat = "Type 2 diabetes risk indicators";
        if (r.conditions().contains("diabetes"))
            return new RiskResult(cat, "NOT_APPLICABLE", null, "Not applicable: diabetes already reported",
                    List.of("You reported an existing diabetes diagnosis"),
                    "Risk screening is for people without diagnosed diabetes.",
                    List.of("Continue regular follow-ups and glucose monitoring with your doctor."), List.of());
        boolean male = ScoreCard.eq(r.sex(), "male"), female = ScoreCard.eq(r.sex(), "female");
        boolean idrs = r.waistCm() != null && (male || female) && r.activityLevel() != null && r.diabetesFamily() != null;
        ScoreCard s = new ScoreCard();
        List<String> missing = new ArrayList<>();
        String level, method;
        if (idrs) {
            method = "Indian Diabetes Risk Score (IDRS), validated screening score for adults";
            s.add(r.age() >= 50 ? 30 : r.age() >= 35 ? 20 : 0, "Age " + r.age());
            double w = r.waistCm();
            s.add(male ? (w >= 100 ? 20 : w >= 90 ? 10 : 0) : (w >= 90 ? 20 : w >= 80 ? 10 : 0), "Waist circumference " + w + " cm");
            s.add(ScoreCard.eq(r.activityLevel(), "SEDENTARY") ? 20 : ScoreCard.eq(r.activityLevel(), "MODERATE") ? 10 : 0,
                    "Physical activity level: " + r.activityLevel().toLowerCase());
            s.add(ScoreCard.eq(r.diabetesFamily(), "BOTH_PARENTS") ? 20 : ScoreCard.eq(r.diabetesFamily(), "ONE_PARENT") ? 10 : 0,
                    "Family history of diabetes: " + r.diabetesFamily().toLowerCase().replace('_', ' '));
            level = s.points < 30 ? "LOW" : s.points <= 50 ? "MODERATE" : "HIGH";
        } else {
            method = "Rule-based indicator (IDRS could not be computed because required inputs are missing)";
            if (r.waistCm() == null) missing.add("Waist circumference");
            if (!male && !female) missing.add("Biological sex (male/female) for IDRS waist cut-offs");
            if (r.diabetesFamily() == null) missing.add("Parental diabetes history");
            if (r.activityLevel() == null) missing.add("Activity level");
            s.add(bmi >= 30 ? 3 : bmi >= 25 ? 2 : 0, "BMI " + String.format("%.1f", bmi) + " above healthy range");
            s.add(r.age() >= 45 ? 2 : 0, "Age " + r.age());
            s.add(ScoreCard.eq(r.diabetesFamily(), "BOTH_PARENTS") ? 3 : ScoreCard.eq(r.diabetesFamily(), "ONE_PARENT") ? 2 : 0, "Family history of diabetes");
            s.add(ScoreCard.eq(r.activityLevel(), "SEDENTARY") ? 2 : 0, "Sedentary activity level");
            s.add(r.symptoms().contains("frequent_thirst") ? 1 : 0, "Reported symptom: frequent thirst");
            s.add(r.symptoms().contains("frequent_urination") ? 1 : 0, "Reported symptom: frequent urination");
            level = s.level(3, 6);
        }
        List<String> next = ScoreCard.steps(level,
                "Maintain a balanced diet and regular activity. Consider a blood sugar test every few years.",
                "Ask a healthcare professional about a fasting blood glucose or HbA1c test.",
                "Please arrange a blood sugar test (fasting glucose or HbA1c) with a doctor soon.");
        return new RiskResult(cat, level, s.points, method, s.factors,
                "Score " + s.points + (idrs ? " out of 100" : "") + ". A screening indicator only; only a blood test can show diabetes.",
                next, missing);
    }
}
