package com.healthverse.report;

import com.healthverse.dto.AssessmentRequest;
import com.healthverse.dto.ReportRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReportServiceTest {
    @Autowired ReportService reports;

    @Test
    void createsValidPdf() {
        AssessmentRequest a = new AssessmentRequest(52, "male", 170.0, 82.0, 102.0, 138, 88, Set.of("hypertension"), "ONE_PARENT", true,
                "SEDENTARY", 5.5, 2, "HIGH_PROCESSED", 1.2, "FORMER", "OCCASIONAL", 7, 9.0, Set.of("fatigue", "chest_pain"));
        byte[] pdf = reports.generate(new ReportRequest(a, "Demo User", "O+", null, null, "1 to 4 weeks", "Mild", null, null, "2026-11-01", true));
        assertTrue(pdf.length > 2000);
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
        assertEquals("healthverse-report-2026-11-01.pdf", reports.fileName(new ReportRequest(a, null, null, null, null, null, null, null, null, "2026-11-01", null)));
    }
}
