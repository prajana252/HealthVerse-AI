package com.healthverse.controller;

import com.healthverse.assessment.HealthRiskEngine;
import com.healthverse.dto.*;
import com.healthverse.service.AssessmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {
    private final HealthRiskEngine engine;
    private final AssessmentService service;

    public AssessmentController(HealthRiskEngine engine, AssessmentService service) {
        this.engine = engine;
        this.service = service;
    }

    /** Analyse without storing anything. */
    @PostMapping("/analyze")
    public AssessmentResult analyze(@Valid @RequestBody AssessmentRequest request) {
        return engine.analyze(request);
    }

    /** Save an assessment. The returned accessCode is the only key to read or delete it. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaveResponse save(@Valid @RequestBody SaveRequest request) {
        return service.save(request);
    }

    @GetMapping("/{id}")
    public SavedAssessment get(@PathVariable String id, @RequestHeader("X-Access-Code") String accessCode) {
        return service.get(id, accessCode);
    }

    /** The browser keeps its own id + accessCode pairs and asks for the matching summaries. */
    @PostMapping("/history")
    public List<AssessmentSummary> history(@RequestBody List<@Valid HistoryEntry> entries) {
        return service.history(entries);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id, @RequestHeader("X-Access-Code") String accessCode) {
        service.delete(id, accessCode);
    }
}
