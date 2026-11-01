package com.healthverse.assessment;

import com.healthverse.dto.AssessmentRequest;
import com.healthverse.dto.RiskResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SleepRiskService implements RiskAssessor {
    @Override
    public RiskResult assess(AssessmentRequest r, double bmi) {
        String cat = "Sleep-related health concerns";
        if (r.sleepHours() == null)
            return new RiskResult(cat, "INSUFFICIENT_DATA", null, "Needs sleep duration", List.of(),
                    "Sleep duration was not provided.", List.of("Enter your average sleep hours to get this assessment."), List.of("Sleep duration"));
        ScoreCard s = new ScoreCard();
        double h = r.sleepHours();
        s.add(h < 6 ? 3 : h < 7 ? 2 : h > 9 ? 1 : 0, "Sleep duration " + h + " h (adults are generally advised 7-9 h)");
        s.add(r.sleepQuality() != null && r.sleepQuality() <= 2 ? 2 : 0, "Poor self-rated sleep quality");
        s.add(r.symptoms().contains("sleep_disturbance") ? 1 : 0, "Reported symptom: sleep disturbances");
        s.add(r.symptoms().contains("fatigue") ? 1 : 0, "Reported symptom: persistent fatigue");
        String level = s.level(1, 3);
        return new RiskResult(cat, level, s.points, "Rule-based indicator score (sleep guidelines for adults)", s.factors,
                "Score " + s.points + ". Observed lifestyle factor, not a sleep disorder diagnosis.",
                ScoreCard.steps(level, "Keep a regular sleep schedule.",
                        "Try a consistent bedtime, less late-evening screen time and a cool dark room.",
                        "Improve sleep habits, and speak to a doctor if tiredness or poor sleep continues."), List.of());
    }
}
