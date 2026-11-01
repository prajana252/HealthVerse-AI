package com.healthverse.service;

import com.healthverse.dto.*;
import com.healthverse.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AssessmentServiceTest {
    @Autowired AssessmentService service;

    private AssessmentRequest sample() {
        return new AssessmentRequest(52, "male", 170.0, 82.0, 102.0, 138, 88, Set.of("hypertension"), "ONE_PARENT", true,
                "SEDENTARY", 5.5, 2, "HIGH_PROCESSED", 1.2, "FORMER", "OCCASIONAL", 7, 9.0, Set.of("fatigue"));
    }

    @Test
    void saveLoadHistoryAndDelete() {
        SaveResponse saved = service.save(new SaveRequest(sample(), "demo run", true));
        SavedAssessment loaded = service.get(saved.id(), saved.accessCode());
        assertEquals(82.0, loaded.request().weightKg());
        assertFalse(loaded.plan().recommendations().isEmpty());
        assertTrue(loaded.plan().dailyGoals().stream().anyMatch(g -> g.contains("Walk")));
        assertEquals(1, service.history(List.of(new HistoryEntry(saved.id(), saved.accessCode()))).size());
        assertThrows(NotFoundException.class, () -> service.get(saved.id(), "wrong-code"));
        service.delete(saved.id(), saved.accessCode());
        assertThrows(NotFoundException.class, () -> service.get(saved.id(), saved.accessCode()));
    }
}
