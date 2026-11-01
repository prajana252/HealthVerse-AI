package com.healthverse.dto;

import java.util.List;

public record Recommendation(String category, String level, String meaning, List<String> contributingFactors,
                             String lifestyle, String diet, String activity, String sleep, String stress,
                             String consultation) {
}
