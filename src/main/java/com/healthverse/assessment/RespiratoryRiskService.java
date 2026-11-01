package com.healthverse.assessment;

import com.healthverse.dto.AssessmentRequest;
import com.healthverse.dto.RiskResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RespiratoryRiskService implements RiskAssessor {
    @Override
    public RiskResult assess(AssessmentRequest r, double bmi) {
        ScoreCard s = new ScoreCard();
        s.add(ScoreCard.eq(r.tobacco(), "CURRENT") ? 4 : ScoreCard.eq(r.tobacco(), "FORMER") ? 1 : 0, "Tobacco use: " + (r.tobacco() == null ? "" : r.tobacco().toLowerCase()));
        s.add(r.symptoms().contains("cough") ? 2 : 0, "Reported symptom: persistent cough");
        s.add(r.symptoms().contains("breathlessness") ? 2 : 0, "Reported symptom: breathlessness");
        s.add(r.symptoms().contains("severe_breathlessness") ? 4 : 0, "Reported symptom: severe breathing difficulty");
        s.add(r.conditions().contains("asthma") ? 2 : 0, "Reported asthma");
        s.add(r.conditions().contains("copd") ? 3 : 0, "Reported COPD");
        s.add(bmi >= 30 ? 1 : 0, "BMI in the obesity range");
        String level = s.level(2, 5);
        List<String> next = new ArrayList<>(ScoreCard.steps(level,
                "Avoid smoke exposure and stay active to support lung health.",
                "If cough or breathlessness continues, consider a check-up and ask whether a breathing test (spirometry) is useful.",
                "Please see a doctor about your breathing and cough, and avoid tobacco smoke."));
        if (r.symptoms().contains("severe_breathlessness"))
            next.add("Severe breathing difficulty needs urgent medical attention.");
        return new RiskResult("Respiratory health indicators", level, s.points, "Rule-based indicator score (not a validated probability)",
                s.factors, "Score " + s.points + " from " + s.factors.size() + " factor(s). Symptoms have many possible causes.",
                next, List.of());
    }
}
