package com.healthverse.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** One saved assessment. Public fields keep this beginner-friendly; JPA maps them directly. */
@Entity
@Table(name = "assessments")
public class HealthAssessment {
    @Id @Column(length = 36) public String id;
    @Column(name = "created_at", nullable = false) public Instant createdAt;
    @Column(length = 60) public String label;
    @Column(name = "access_code_hash", nullable = false, length = 64) public String accessCodeHash;
    @Column(name = "overall_indicator", length = 20) public String overallIndicator;
    public Double bmi;
    @Column(name = "risk_factor_count") public int riskFactorCount;
    public boolean demo;
    @Column(name = "request_json", columnDefinition = "TEXT") public String requestJson;
    @Column(name = "result_json", columnDefinition = "TEXT") public String resultJson;
    @Column(name = "plan_json", columnDefinition = "TEXT") public String planJson;
    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<RiskResultEntity> risks = new ArrayList<>();
    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<RecommendationEntity> recommendations = new ArrayList<>();
}
