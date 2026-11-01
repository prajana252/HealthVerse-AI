package com.healthverse.model;

import jakarta.persistence.*;

@Entity
@Table(name = "risk_results")
public class RiskResultEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "assessment_id") public HealthAssessment assessment;
    @Column(length = 80) public String category;
    @Column(name = "risk_level", length = 20) public String level;
    @Column(name = "risk_score") public Integer score;
}
