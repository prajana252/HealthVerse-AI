package com.healthverse.recommendation;

import com.healthverse.dto.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Builds advice only for categories rated MODERATE or HIGH. Never suggests changing medication. */
@Service
public class RecommendationService {
    private record Advice(String meaning, String lifestyle, String diet, String activity, String sleep, String stress, String consult) { }

    private static final String MED = " Do not start, stop or change any prescribed medicine without medical advice.";

    public RecommendationPlan generate(AssessmentRequest r, AssessmentResult res) {
        List<Recommendation> items = new ArrayList<>();
        for (RiskResult risk : res.risks()) {
            if (!"MODERATE".equals(risk.level()) && !"HIGH".equals(risk.level())) continue;
            Advice a = advice(risk.category());
            items.add(new Recommendation(risk.category(), risk.level(), a.meaning(), risk.factors(),
                    a.lifestyle(), a.diet(), a.activity(), a.sleep(), a.stress(), a.consult()));
        }
        return new RecommendationPlan(items, dailyGoals(r), weeklyGoals(r, res));
    }

    private Advice advice(String c) {
        if (c.startsWith("Cardio")) return new Advice(
                "Several factors linked with heart and blood-vessel health were identified. This is a screening indicator, not a diagnosis.",
                "Avoid tobacco, limit alcohol and work towards a healthy weight.",
                "Favour vegetables, fruit, whole grains, pulses and nuts; reduce salt, fried and processed foods.",
                "Build gradually towards about 150 minutes of moderate activity a week, such as brisk walking.",
                "Aim for 7-9 hours; poor sleep is linked with higher blood pressure.",
                "Practise slow breathing or relaxation daily.",
                "Ask a healthcare professional about blood pressure, cholesterol and blood sugar checks." + MED);
        if (c.startsWith("Type 2")) return new Advice(
                "Your answers match several known risk factors for type 2 diabetes. Only a blood test can show whether you have it.",
                "Keep a healthy weight and avoid tobacco.",
                "Cut sugary drinks and sweets, choose whole grains and pulses, and watch portion sizes.",
                "Walk after meals and work up to 150 minutes of moderate activity weekly.",
                "Regular, sufficient sleep supports healthy blood sugar.",
                "Chronic stress can affect blood sugar; schedule daily relaxation.",
                "Ask about a fasting glucose or HbA1c test." + MED);
        if (c.startsWith("Respiratory")) return new Advice(
                "Factors or symptoms related to lung health were identified. Many causes are minor and treatable.",
                "Avoid tobacco smoke, dust and heavy air pollution where you can.",
                "Eat plenty of fruit and vegetables and stay hydrated.",
                "Gentle regular activity such as walking helps lung capacity; stop and rest if breathless.",
                "Sleep with your head slightly raised if night cough bothers you.",
                "Slow diaphragmatic breathing can ease tension.",
                "See a doctor if cough or breathlessness lasts more than a few weeks." + MED);
        if (c.startsWith("Sleep")) return new Advice(
                "Your sleep pattern is outside the commonly advised range for adults (7-9 hours).",
                "Keep the same bedtime and wake time every day, including weekends.",
                "Avoid caffeine in the afternoon and heavy meals close to bedtime.",
                "Regular daytime activity improves sleep, but avoid hard exercise right before bed.",
                "Keep the room dark, cool and quiet, and put screens away an hour before bed.",
                "Write down worries earlier in the evening so they do not follow you to bed.",
                "Speak to a doctor if poor sleep or daytime tiredness continues for weeks." + MED);
        if (c.startsWith("Stress")) return new Advice(
                "Your stress, screen time or activity pattern may be affecting your wellbeing. These are lifestyle observations, not conditions.",
                "Take regular breaks, limit alcohol and make time for friends and hobbies.",
                "Eat regular meals and avoid relying on caffeine or sugar for energy.",
                "Even 20-30 minutes of walking daily reduces stress and sitting time.",
                "Protect your sleep window; it supports mood.",
                "Try breathing exercises, meditation, journaling or talking to someone you trust.",
                "If stress feels overwhelming or lasts long, consider a counsellor or doctor." + MED);
        return new Advice(
                "Your diet, hydration or weight pattern may be improved. This is based only on self-reported answers.",
                "Eat at regular times and plan meals ahead.",
                "Aim for a varied plate: vegetables, fruit, pulses, whole grains and lean protein; limit processed and sugary foods.",
                "Pair healthier eating with regular movement.",
                "Avoid late-night snacking, which can disturb sleep.",
                "Notice emotional eating and replace it with a short walk or breathing exercise.",
                "A registered dietitian can build a plan that suits your needs." + MED);
    }

    private List<String> dailyGoals(AssessmentRequest r) {
        List<String> g = new ArrayList<>();
        if (r.waterLitres() != null && r.waterLitres() < 1.5)
            g.add("Drink about 2 litres of water through the day (unless a doctor has limited your fluids).");
        if (r.sleepHours() != null && r.sleepHours() < 7)
            g.add("Go to bed at a fixed time and aim for 7-9 hours of sleep.");
        if (r.screenHours() != null && r.screenHours() >= 5)
            g.add("Stop screens 1 hour before bed and take a short break every 30 minutes of screen use.");
        if ("SEDENTARY".equalsIgnoreCase(r.activityLevel()))
            g.add("Walk for 20-30 minutes and stand up to move every hour.");
        if (r.stress() != null && r.stress() >= 6)
            g.add("Spend 10 minutes on slow breathing, stretching or a hobby you enjoy.");
        if ("CURRENT".equalsIgnoreCase(r.tobacco()))
            g.add("Track your tobacco use and set a lower daily limit; ask a clinic about quit support.");
        if (r.diet() != null && !"BALANCED".equalsIgnoreCase(r.diet()))
            g.add("Eat at least 2 servings of vegetables and 1 of fruit, and swap one processed item for a whole food.");
        if (g.isEmpty()) g.add("Keep up your current routine: stay active, hydrated and well rested.");
        return g;
    }

    private List<String> weeklyGoals(AssessmentRequest r, AssessmentResult res) {
        List<String> g = new ArrayList<>();
        if (!"VIGOROUS".equalsIgnoreCase(r.activityLevel()))
            g.add("Build up to 150 minutes of moderate activity this week (for example 5 brisk 30-minute walks).");
        if (r.systolic() == null || r.diastolic() == null)
            g.add("Get your blood pressure checked at a clinic or pharmacy.");
        if (res.risks().stream().anyMatch(x -> x.category().startsWith("Type 2")
                && ("MODERATE".equals(x.level()) || "HIGH".equals(x.level()))))
            g.add("Ask a healthcare professional about a fasting glucose or HbA1c test.");
        if (!"LOW".equals(res.overallIndicator()))
            g.add("Book a check-up to discuss this report with a healthcare professional.");
        g.add("Cook healthy meals at home on at least 4 days.");
        return g;
    }
}
