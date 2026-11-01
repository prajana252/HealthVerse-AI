package com.healthverse.controller;

import com.healthverse.assessment.HealthRiskEngine;
import com.healthverse.dto.AssessmentRequest;
import com.healthverse.dto.RecommendationPlan;
import com.healthverse.recommendation.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final HealthRiskEngine engine;
    private final RecommendationService recommendations;

    public RecommendationController(HealthRiskEngine engine, RecommendationService recommendations) {
        this.engine = engine;
        this.recommendations = recommendations;
    }

    @PostMapping
    public RecommendationPlan generate(@Valid @RequestBody AssessmentRequest request) {
        return recommendations.generate(request, engine.analyze(request));
    }
}
