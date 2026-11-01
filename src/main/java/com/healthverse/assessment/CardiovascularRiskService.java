package com.healthverse.assessment;

import com.healthverse.dto.AssessmentRequest;
import com.healthverse.dto.RiskResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Rule-based risk-factor count. NOT Framingham/ASCVD: cholesterol is not collected. */
@Service
public class CardiovascularRiskService implements RiskAssessor {
    @Override
    public RiskResult assess(AssessmentRequest r, double bmi) {
        ScoreCard s = new ScoreCard();
        List<String> missing = new ArrayList<>();
        int cut = ScoreCard.eq(r.sex(), "male") ? 45 : ScoreCard.eq(r.sex(), "female") ? 55 : 50;
        if (r.age() >= cut) s.add(2, "Age " + r.age() + " is in a higher-risk age band");
        if (bmi >= 30) s.add(2, "BMI " + String.format("%.1f", bmi) + " is in the obesity range");
        else if (bmi >= 25) s.add(1, "BMI " + String.format("%.1f", bmi) + " is above the healthy range");
        if (ScoreCard.eq(r.tobacco(), "CURRENT")) s.add(3, "Current tobacco use");
        else if (ScoreCard.eq(r.tobacco(), "FORMER")) s.add(1, "Past tobacco use");
        if (r.systolic() == null || r.diastolic() == null) missing.add("Blood pressure");
        else if (r.systolic() >= 140 || r.diastolic() >= 90)
            s.add(3, "Blood pressure " + r.systolic() + "/" + r.diastolic() + " is in the high range");
        else if (r.systolic() >= 130 || r.diastolic() >= 80)
            s.add(2, "Blood pressure " + r.systolic() + "/" + r.diastolic() + " is mildly raised");
        if (r.conditions().contains("diabetes")) s.add(2, "Reported diabetes");
        if (r.conditions().contains("hypertension")) s.add(2, "Reported hypertension");
        if (r.conditions().contains("heart_disease")) s.add(3, "Reported heart condition");
        if (Boolean.TRUE.equals(r.heartFamily())) s.add(2, "Family history of heart disease");
        if (ScoreCard.eq(r.activityLevel(), "SEDENTARY")) s.add(2, "Sedentary activity level");
        if (r.symptoms().contains("chest_pain")) s.add(1, "Reported symptom: chest discomfort");
        if (r.symptoms().contains("breathlessness")) s.add(1, "Reported symptom: breathlessness");
        String level = s.level(3, 7);
        List<String> next = new ArrayList<>(ScoreCard.steps(level,
                "Keep up heart-healthy habits and get a routine blood pressure check at least yearly.",
                "Discuss blood pressure, cholesterol and blood sugar checks with a healthcare professional.",
                "Please book a check-up with a doctor soon to discuss heart-health screening."));
        if (r.symptoms().contains("chest_pain"))
            next.add("Chest discomfort can be serious. Seek medical attention promptly, and call emergency services if it is severe or sudden.");
        return new RiskResult("Cardiovascular health indicators", level, s.points,
                "Rule-based risk-factor count (not a validated probability; cholesterol not collected)",
                s.factors, "Score " + s.points + " from " + s.factors.size()
                + " contributing factor(s). This is a screening indicator, not a diagnosis.", next, missing);
    }
}
