package com.healthverse.report;

import com.healthverse.assessment.HealthRiskEngine;
import com.healthverse.dto.*;
import com.healthverse.recommendation.RecommendationService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Creates the PDF with OpenPDF. The PDF uses a basic Latin font, so non-Latin text (for example Tamil) will not display. */
@Service
public class ReportService {
    private static final Color NAVY = new Color(25, 35, 110);
    private static final Font TITLE = new Font(Font.HELVETICA, 20, Font.BOLD, NAVY);
    private static final Font H2 = new Font(Font.HELVETICA, 13, Font.BOLD, NAVY);
    private static final Font H3 = new Font(Font.HELVETICA, 10.5f, Font.BOLD, Color.BLACK);
    private static final Font BODY = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
    private static final Font SMALL = new Font(Font.HELVETICA, 8.5f, Font.NORMAL, Color.GRAY);
    private static final Font WARN = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(190, 30, 50));

    private final HealthRiskEngine engine;
    private final RecommendationService recommendations;
    private final MethodologyService methodology;

    public ReportService(HealthRiskEngine engine, RecommendationService recommendations, MethodologyService methodology) {
        this.engine = engine;
        this.recommendations = recommendations;
        this.methodology = methodology;
    }

    public String fileName(ReportRequest rq) {
        return "healthverse-report-" + date(rq) + ".pdf";
    }

    public byte[] generate(ReportRequest rq) {
        AssessmentRequest r = rq.assessment();
        AssessmentResult res = engine.analyze(r);
        RecommendationPlan plan = recommendations.generate(r, res);
        Methodology m = methodology.build(r, res);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 48, 48, 54, 54);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(para("HEALTHVERSE AI", TITLE, 0, 0));
            doc.add(para("Health snapshot and transparency report", H3, 0, 4));
            doc.add(para("Assessment date: " + date(rq), SMALL, 0, 4));
            if (Boolean.TRUE.equals(rq.demo())) doc.add(para("DEMO DATA: fictional sample information, not real health data.", WARN, 0, 4));
            doc.add(para("Educational screening tool. It does not diagnose disease or predict that you will develop any condition.", SMALL, 0, 6));
            if (res.emergencyWarning() != null) doc.add(para("URGENT: " + res.emergencyWarning(), WARN, 4, 6));

            doc.add(para("Personal profile summary", H2, 10, 4));
            Map<String, String> profile = new LinkedHashMap<>();
            profile.put("Name", rq.name());
            profile.put("Age", r.age() + " years");
            profile.put("Biological sex", r.sex());
            profile.put("Height / weight", r.heightCm() + " cm / " + r.weightKg() + " kg");
            profile.put("Waist circumference", r.waistCm() == null ? null : r.waistCm() + " cm");
            profile.put("Blood pressure", r.systolic() == null || r.diastolic() == null ? null : r.systolic() + "/" + r.diastolic() + " mmHg");
            profile.put("Blood group", rq.bloodGroup());
            profile.put("Reported conditions", r.conditions().isEmpty() ? "None reported" : String.join(", ", r.conditions()));
            profile.put("Medications", rq.medications());
            profile.put("Allergies", rq.allergies());
            profile.put("Family history", "Diabetes in parents: " + nice(r.diabetesFamily()) + "; heart disease: " + (Boolean.TRUE.equals(r.heartFamily()) ? "yes" : "no/not reported"));
            doc.add(kv(profile));

            BmiResponse b = res.bmi();
            doc.add(para("BMI result", H2, 10, 4));
            doc.add(para("BMI " + b.bmi() + " - " + b.category() + ". Healthy BMI range " + b.healthyBmiMin() + "-" + b.healthyBmiMax()
                    + " (about " + b.healthyWeightMinKg() + "-" + b.healthyWeightMaxKg() + " kg for your height).", BODY, 0, 2));
            doc.add(para(b.explanation() + " " + b.disclaimer(), SMALL, 0, 4));

            doc.add(para("Overall assessment", H2, 10, 4));
            doc.add(para("Overall attention level: " + res.overallIndicator() + ". " + res.riskFactorCount() + " contributing factor(s) were identified.", BODY, 0, 4));

            doc.add(para("Lifestyle analysis", H2, 10, 4));
            Map<String, String> life = new LinkedHashMap<>();
            life.put("Physical activity", nice(r.activityLevel()));
            life.put("Sleep", r.sleepHours() == null ? null : r.sleepHours() + " h, quality " + r.sleepQuality() + "/5");
            life.put("Diet", nice(r.diet()));
            life.put("Water intake", r.waterLitres() == null ? null : r.waterLitres() + " L/day");
            life.put("Tobacco", nice(r.tobacco()));
            life.put("Alcohol", nice(r.alcohol()));
            life.put("Stress", r.stress() == null ? null : r.stress() + "/10");
            life.put("Screen and sitting time", r.screenHours() == null ? null : r.screenHours() + " h/day");
            doc.add(kv(life));

            doc.add(para("Symptom summary", H2, 10, 4));
            Map<String, String> sym = new LinkedHashMap<>();
            sym.put("Reported symptoms", r.symptoms().isEmpty() ? "None selected" : String.join(", ", r.symptoms().stream().map(this::nice).toList()));
            sym.put("Duration", rq.duration());
            sym.put("Severity", rq.severity());
            sym.put("Frequency", rq.frequency());
            sym.put("Recent changes / history", rq.recentChanges());
            doc.add(kv(sym));
            doc.add(para("A symptom alone does not point to a particular disease.", SMALL, 2, 4));

            doc.add(para("Individual health risk assessments", H2, 10, 4));
            for (RiskResult x : res.risks()) {
                doc.add(para(x.category() + ": " + x.level().replace('_', ' ') + (x.score() == null ? "" : " (score " + x.score() + ")"), H3, 6, 1));
                doc.add(para("Method: " + x.method(), SMALL, 0, 2));
                for (String f : x.factors()) doc.add(bullet(f));
                doc.add(para(x.explanation(), BODY, 2, 1));
                for (String s : x.nextSteps()) doc.add(bullet("Next step: " + s));
            }

            Set<String> factors = new LinkedHashSet<>();
            res.risks().forEach(x -> factors.addAll(x.factors()));
            doc.add(para("Main contributing factors", H2, 10, 4));
            if (factors.isEmpty()) doc.add(para("No contributing factors were identified.", BODY, 0, 2));
            factors.stream().limit(12).forEach(f -> addSafe(doc, bullet(f)));

            doc.add(para("Personalised recommendations", H2, 10, 4));
            if (plan.recommendations().isEmpty()) doc.add(para("No category was rated Moderate or High. Keep up your current habits.", BODY, 0, 2));
            for (Recommendation rec : plan.recommendations()) {
                doc.add(para(rec.category() + " (" + rec.level() + ")", H3, 6, 1));
                doc.add(para(rec.meaning(), BODY, 0, 1));
                doc.add(bullet("Lifestyle: " + rec.lifestyle()));
                doc.add(bullet("Diet: " + rec.diet()));
                doc.add(bullet("Activity: " + rec.activity()));
                doc.add(bullet("Sleep: " + rec.sleep()));
                doc.add(bullet("Stress: " + rec.stress()));
                doc.add(bullet("Professional advice: " + rec.consultation()));
            }
            doc.add(para("Suggested goals", H3, 8, 1));
            for (String g : plan.dailyGoals()) doc.add(bullet("Daily: " + g));
            for (String g : plan.weeklyGoals()) doc.add(bullet("Weekly: " + g));

            doc.add(para("HOW YOUR RESULTS WERE GENERATED", H2, 14, 4));
            section(doc, "Information considered", m.consideredInputs());
            section(doc, "Information missing", m.notProvided());
            section(doc, "Assessment rules applied", m.rulesApplied());
            section(doc, "How risk categories were determined", m.categoryDetermination());
            section(doc, "What this system cannot determine", m.cannotDetermine());
            section(doc, "Why this is not a medical diagnosis", m.notADiagnosis());
            doc.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("Could not create PDF", e);
        }
        return out.toByteArray();
    }

    private void addSafe(Document doc, Paragraph p) {
        try { doc.add(p); } catch (DocumentException e) { throw new IllegalStateException(e); }
    }

    private void section(Document doc, String title, List<String> items) throws DocumentException {
        doc.add(para(title, H3, 6, 1));
        for (String s : items) doc.add(bullet(s));
    }

    private Paragraph para(String text, Font f, float before, float after) {
        Paragraph p = new Paragraph(text, f);
        p.setSpacingBefore(before);
        p.setSpacingAfter(after);
        return p;
    }

    private Paragraph bullet(String text) {
        Paragraph p = new Paragraph("- " + text, BODY);
        p.setIndentationLeft(12);
        return p;
    }

    private PdfPTable kv(Map<String, String> rows) throws DocumentException {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{32, 68});
        rows.forEach((k, v) -> {
            if (v == null || v.isBlank()) return;
            t.addCell(cell(k, H3));
            t.addCell(cell(v, BODY));
        });
        return t;
    }

    private PdfPCell cell(String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setPadding(4);
        c.setBorderColor(Color.LIGHT_GRAY);
        return c;
    }

    private String nice(String v) {
        return v == null || v.isBlank() ? "not answered" : v.toLowerCase().replace('_', ' ');
    }

    private String date(ReportRequest rq) {
        try { return LocalDate.parse(rq.assessmentDate()).toString(); }
        catch (Exception e) { return LocalDate.now().toString(); }
    }
}
