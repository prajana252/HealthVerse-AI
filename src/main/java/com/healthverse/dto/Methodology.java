package com.healthverse.dto;

import java.util.List;

public record Methodology(List<String> consideredInputs, List<String> notProvided, List<String> rulesApplied,
                          List<String> categoryDetermination, List<String> cannotDetermine, List<String> notADiagnosis) {
}
