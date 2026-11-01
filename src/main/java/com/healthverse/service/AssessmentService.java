package com.healthverse.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthverse.assessment.HealthRiskEngine;
import com.healthverse.dto.*;
import com.healthverse.exception.NotFoundException;
import com.healthverse.model.*;
import com.healthverse.recommendation.RecommendationService;
import com.healthverse.repository.HealthAssessmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

@Service
public class AssessmentService {
    private final HealthRiskEngine engine;
    private final RecommendationService recommendations;
    private final HealthAssessmentRepository repo;
    private final ObjectMapper mapper;
    private final SecureRandom random = new SecureRandom();

    public AssessmentService(HealthRiskEngine engine, RecommendationService recommendations,
                             HealthAssessmentRepository repo, ObjectMapper mapper) {
        this.engine = engine;
        this.recommendations = recommendations;
        this.repo = repo;
        this.mapper = mapper;
    }

    @Transactional
    public SaveResponse save(SaveRequest req) {
        AssessmentResult result = engine.analyze(req.assessment());
        RecommendationPlan plan = recommendations.generate(req.assessment(), result);
        byte[] raw = new byte[24];
        random.nextBytes(raw);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        HealthAssessment a = new HealthAssessment();
        a.id = UUID.randomUUID().toString();
        a.createdAt = Instant.now();
        a.label = req.label();
        a.accessCodeHash = hash(code);
        a.overallIndicator = result.overallIndicator();
        a.bmi = result.bmi().bmi();
        a.riskFactorCount = result.riskFactorCount();
        a.demo = Boolean.TRUE.equals(req.demo());
        a.requestJson = toJson(req.assessment());
        a.resultJson = toJson(result);
        a.planJson = toJson(plan);
        for (RiskResult r : result.risks()) {
            RiskResultEntity e = new RiskResultEntity();
            e.assessment = a; e.category = r.category(); e.level = r.level(); e.score = r.score();
            a.risks.add(e);
        }
        for (Recommendation rec : plan.recommendations()) {
            RecommendationEntity e = new RecommendationEntity();
            e.assessment = a; e.category = rec.category(); e.level = rec.level(); e.meaning = rec.meaning();
            a.recommendations.add(e);
        }
        repo.save(a);
        return new SaveResponse(a.id, code, a.createdAt);
    }

    @Transactional(readOnly = true)
    public SavedAssessment get(String id, String accessCode) {
        HealthAssessment a = authorised(id, accessCode);
        return new SavedAssessment(a.id, a.createdAt, a.label, a.demo,
                fromJson(a.requestJson, AssessmentRequest.class),
                fromJson(a.resultJson, AssessmentResult.class),
                fromJson(a.planJson, RecommendationPlan.class));
    }

    @Transactional(readOnly = true)
    public List<AssessmentSummary> history(List<HistoryEntry> entries) {
        List<AssessmentSummary> out = new ArrayList<>();
        for (HistoryEntry e : entries) {
            repo.findById(e.id()).filter(a -> matches(a, e.accessCode())).ifPresent(a ->
                    out.add(new AssessmentSummary(a.id, a.createdAt, a.label, a.overallIndicator, a.bmi, a.riskFactorCount, a.demo)));
        }
        out.sort(Comparator.comparing(AssessmentSummary::createdAt).reversed());
        return out;
    }

    @Transactional
    public void delete(String id, String accessCode) {
        repo.delete(authorised(id, accessCode));
    }

    private HealthAssessment authorised(String id, String code) {
        return repo.findById(id).filter(a -> matches(a, code)).orElseThrow(NotFoundException::new);
    }

    private boolean matches(HealthAssessment a, String code) {
        return code != null && MessageDigest.isEqual(
                a.accessCodeHash.getBytes(StandardCharsets.UTF_8), hash(code).getBytes(StandardCharsets.UTF_8));
    }

    private String hash(String s) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private String toJson(Object o) {
        try { return mapper.writeValueAsString(o); }
        catch (JsonProcessingException e) { throw new IllegalStateException("Could not serialise assessment", e); }
    }

    private <T> T fromJson(String json, Class<T> type) {
        try { return mapper.readValue(json, type); }
        catch (JsonProcessingException e) { throw new IllegalStateException("Stored assessment is unreadable", e); }
    }
}
