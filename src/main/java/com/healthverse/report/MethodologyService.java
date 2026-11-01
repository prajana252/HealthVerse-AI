package com.healthverse.report;

import com.healthverse.dto.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Builds the "How your results were generated" section from the actual inputs and results. */
@Service
public class MethodologyService {

    public Methodology build(AssessmentRequest r, AssessmentResult res) {
        List<String> used = new ArrayList<>(), missing = new ArrayList<>();
        used.add("Age: " + r.age());
        used.add("Height and weight, used to calculate BMI " + res.bmi().bmi() + " (WHO adult categories)");
        boolean sexKnown = "male".equalsIgnoreCase(r.sex()) || "female".equalsIgnoreCase(r.sex());
        check(sexKnown, used, missing, "Biological sex (used for age bands and waist cut-offs)", "Biological sex (male or female) for sex-specific cut-offs");
        check(r.waistCm() != null, used, missing, "Waist circumference", "Waist circumference");
        check(r.systolic() != null && r.diastolic() != null, used, missing, "Blood pressure reading", "Blood pressure reading");
        check(r.diabetesFamily() != null, used, missing, "Parental history of diabetes", "Parental history of diabetes");
        check(r.activityLevel() != null, used, missing, "Physical activity level", "Physical activity level");
        check(r.sleepHours() != null, used, missing, "Sleep duration", "Sleep duration");
        check(r.sleepQuality() != null, used, missing, "Sleep quality", "Sleep quality");
        check(r.diet() != null, used, missing, "Dietary habits", "Dietary habits");
        check(r.waterLitres() != null, used, missing, "Daily water intake", "Daily water intake");
        check(r.tobacco() != null, used, missing, "Tobacco use", "Tobacco use");
        check(r.alcohol() != null, used, missing, "Alcohol use", "Alcohol use");
        check(r.stress() != null, used, missing, "Stress level", "Stress level");
        check(r.screenHours() != null, used, missing, "Screen and sitting time", "Screen and sitting time");
        used.add(r.conditions().isEmpty() ? "No existing conditions were reported" : "Reported conditions: " + String.join(", ", r.conditions()));
        if (Boolean.TRUE.equals(r.heartFamily())) used.add("Family history of heart disease");
        used.add(r.symptoms().isEmpty() ? "No symptoms were selected" : r.symptoms().size() + " reported symptom(s)");
        missing.add("Cholesterol, blood sugar and other laboratory results (not collected)");
        missing.add("Medications, allergies, symptom duration and severity (shown in your report but not used in scoring)");

        List<String> rules = new ArrayList<>();
        for (RiskResult x : res.risks()) rules.add(x.category() + ": " + x.method());
        rules.add("Cardiovascular points: Low 0-3, Moderate 4-7, High 8 or more");
        rules.add("Diabetes (IDRS): Low below 30, Moderate 30-50, High 60 or more. Fallback indicator: Low 0-3, Moderate 4-6, High 7 or more");
        rules.add("Respiratory points: Low 0-2, Moderate 3-5, High 6 or more");
        rules.add("Sleep points: Low 0-1, Moderate 2-3, High 4 or more");
        rules.add("Stress and lifestyle points: Low 0-2, Moderate 3-5, High 6 or more");
        rules.add("Nutrition points: Low 0-1, Moderate 2-3, High 4 or more");

        List<String> how = List.of(
                "Each category adds points for each risk factor found in your answers, and every point is listed with its reason.",
                "The total is compared with fixed thresholds to give Low, Moderate or High.",
                "The overall attention level is High if two or more categories are High, Moderate if one is High or two or more are Moderate, otherwise Low.",
                "A category is Not applicable or Insufficient data when its required inputs or eligibility conditions are not met.",
                "Only the diabetes score is based on a published screening tool. The other thresholds are simple educational rules, not clinical cut-offs.");
        List<String> cannot = List.of(
                "Whether you have or will develop any disease",
                "Blood sugar, cholesterol or blood pressure unless you entered a reading",
                "The effect of your medicines, genetics beyond the family history you gave, pregnancy, or ethnicity-specific BMI thresholds",
                "Anything that needs a physical examination or laboratory tests");
        List<String> not = List.of(
                "HEALTHVERSE AI is an educational screening and awareness tool.",
                "Results are indicators based on self-reported information, not diagnoses or predictions.",
                "Please discuss your results with a qualified healthcare professional. Seek urgent care for chest pain, severe breathlessness or fainting.");
        return new Methodology(used, missing, rules, how, cannot, not);
    }

    private void check(boolean ok, List<String> used, List<String> missing, String usedText, String missingText) {
        if (ok) used.add(usedText); else missing.add(missingText);
    }
}
