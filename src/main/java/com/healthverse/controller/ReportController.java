package com.healthverse.controller;

import com.healthverse.assessment.HealthRiskEngine;
import com.healthverse.dto.AssessmentRequest;
import com.healthverse.dto.Methodology;
import com.healthverse.dto.ReportRequest;
import com.healthverse.report.MethodologyService;
import com.healthverse.report.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reports;
    private final MethodologyService methodology;
    private final HealthRiskEngine engine;

    public ReportController(ReportService reports, MethodologyService methodology, HealthRiskEngine engine) {
        this.reports = reports;
        this.methodology = methodology;
        this.engine = engine;
    }

    @PostMapping("/pdf")
    public ResponseEntity<byte[]> pdf(@Valid @RequestBody ReportRequest request) {
        byte[] bytes = reports.generate(request);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + reports.fileName(request) + "\"")
                .body(bytes);
    }

    @PostMapping("/methodology")
    public Methodology methodology(@Valid @RequestBody AssessmentRequest request) {
        return methodology.build(request, engine.analyze(request));
    }
}
