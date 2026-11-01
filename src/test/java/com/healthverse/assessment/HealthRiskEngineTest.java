package com.healthverse.assessment;

import com.healthverse.dto.AssessmentRequest;
import com.healthverse.dto.AssessmentResult;
import com.healthverse.service.BmiCalculatorService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HealthRiskEngineTest {
    private AssessmentRequest req(int age, String sex, double waist, String act, String fam, Set<String> symptoms) {
        return new AssessmentRequest(age, sex, 170.0, 80.0, waist, null, null, Set.of(), fam, false, act,
                7.0, 3, "BALANCED", 2.0, "NEVER", "NONE", 4, 3.0, symptoms);
    }

    @Test
    void idrsHighScoreIs90() {
        var res = new DiabetesRiskService().assess(req(52, "male", 102, "SEDENTARY", "BOTH_PARENTS", Set.of()), 27.7);
        assertEquals(90, res.score());
        assertEquals("HIGH", res.level());
    }

    @Test
    void idrsLowScoreIsZero() {
        var res = new DiabetesRiskService().assess(req(25, "female", 70, "VIGOROUS", "NONE", Set.of()), 22.0);
        assertEquals(0, res.score());
        assertEquals("LOW", res.level());
    }

    @Test
    void chestPainTriggersEmergencyWarning() {
        var engine = new HealthRiskEngine(new BmiCalculatorService(),
                List.of(new CardiovascularRiskService(), new DiabetesRiskService()));
        AssessmentResult out = engine.analyze(req(40, "male", 90, "MODERATE", "NONE", Set.of("chest_pain")));
        assertNotNull(out.emergencyWarning());
        assertTrue(out.missingInputs().contains("Blood pressure"));
    }
}
