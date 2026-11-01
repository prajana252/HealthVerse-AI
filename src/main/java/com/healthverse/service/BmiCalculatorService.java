package com.healthverse.service;

import com.healthverse.dto.BmiRequest;
import com.healthverse.dto.BmiResponse;
import org.springframework.stereotype.Service;

/** Adult BMI calculation using WHO categories. BMI = weight(kg) / height(m)^2. */
@Service
public class BmiCalculatorService {

    private static final double HEALTHY_MIN = 18.5;
    private static final double HEALTHY_MAX = 24.9;

    public BmiResponse calculate(BmiRequest req) {
        double heightM = req.heightCm() / 100.0;
        double bmi = req.weightKg() / (heightM * heightM);
        String category = categorize(bmi);
        return new BmiResponse(
                round(bmi),
                category,
                explain(category),
                HEALTHY_MIN,
                HEALTHY_MAX,
                round(HEALTHY_MIN * heightM * heightM),
                round(HEALTHY_MAX * heightM * heightM),
                "BMI is a screening measure, not a diagnosis. It does not account for muscle mass, "
                        + "age, sex, ethnicity or fat distribution. Adult categories only.");
    }

    public String categorize(double bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal weight";
        if (bmi < 30.0) return "Overweight";
        if (bmi < 35.0) return "Obesity (Class I)";
        if (bmi < 40.0) return "Obesity (Class II)";
        return "Obesity (Class III)";
    }

    private String explain(String category) {
        return switch (category) {
            case "Underweight" -> "Your BMI is below the usual healthy range. A healthcare professional can help check whether this is right for you.";
            case "Normal weight" -> "Your BMI is within the commonly used healthy range for adults.";
            case "Overweight" -> "Your BMI is above the usual healthy range. Small lifestyle changes can help; this is a screening indicator only.";
            default -> "Your BMI is well above the usual healthy range. Consider discussing weight and related health checks with a healthcare professional.";
        };
    }

    private double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
