package com.healthverse.model;

import jakarta.persistence.*;

@Entity
@Table(name = "recommendations")
public class RecommendationEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "assessment_id") public HealthAssessment assessment;
    @Column(length = 80) public String category;
    @Column(name = "risk_level", length = 20) public String level;
    @Column(length = 600) public String meaning;
}
