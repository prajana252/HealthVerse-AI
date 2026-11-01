package com.healthverse.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Optional text fields only appear in the PDF; they are never stored on the server. */
public record ReportRequest(@NotNull @Valid AssessmentRequest assessment,
                            @Size(max = 80) String name, @Size(max = 10) String bloodGroup,
                            @Size(max = 200) String medications, @Size(max = 200) String allergies,
                            @Size(max = 40) String duration, @Size(max = 40) String severity,
                            @Size(max = 40) String frequency, @Size(max = 300) String recentChanges,
                            String assessmentDate, Boolean demo) {
}
