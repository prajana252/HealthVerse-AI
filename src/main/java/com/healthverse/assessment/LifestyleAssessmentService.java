package com.healthverse.assessment;

import com.healthverse.dto.AssessmentRequest;
import com.healthverse.dto.RiskResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Covers stress and lifestyle-related concerns. */
@Service
public class LifestyleAssessmentService implements RiskAssessor {
    @Override
    public RiskResult assess(AssessmentRequest r, double bmi) {
        ScoreCard s = new ScoreCard();
        List<String> missing = new ArrayList<>();
        if (r.stress() == null) missing.add("Stress level");
        else s.add(r.stress() >= 8 ? 3 : r.stress() >= 6 ? 2 : 0, "Self-rated stress " + r.stress() + "/10");
        if (r.screenHours() == null) missing.add("Screen time");
        else s.add(r.screenHours() >= 8 ? 2 : r.screenHours() >= 5 ? 1 : 0, "Screen time " + r.screenHours() + " h/day");
        s.add(ScoreCard.eq(r.alcohol(), "FREQUENT") ? 2 : 0, "Frequent alcohol consumption");
        s.add(ScoreCard.eq(r.activityLevel(), "SEDENTARY") ? 2 : ScoreCard.eq(r.activityLevel(), "MODERATE") ? 0 : 0, "Sedentary activity level");
        String level = s.level(2, 5);
        return new RiskResult("Stress & lifestyle concerns", level, s.points, "Rule-based indicator score", s.factors,
                "Score " + s.points + ". Observed lifestyle factors, not medical conditions.",
                ScoreCard.steps(level, "Your lifestyle indicators look balanced. Keep it up.",
                        "Add short daily movement breaks and a stress-relief routine such as walking or breathing exercises.",
                        "Prioritise stress management and reduced sitting time; consider talking to a counsellor or doctor."), missing);
    }
}
