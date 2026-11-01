package com.healthverse.assessment;

import java.util.ArrayList;
import java.util.List;

/** Small helper that accumulates points and remembers WHY each point was added. */
final class ScoreCard {
    int points;
    final List<String> factors = new ArrayList<>();

    ScoreCard add(int p, String factor) {
        if (p > 0) { points += p; factors.add(factor); }
        return this;
    }

    String level(int lowMax, int moderateMax) {
        return points <= lowMax ? "LOW" : points <= moderateMax ? "MODERATE" : "HIGH";
    }

    static boolean eq(String a, String b) { return a != null && a.equalsIgnoreCase(b); }

    static List<String> steps(String level, String low, String moderate, String high) {
        return List.of(switch (level) { case "LOW" -> low; case "MODERATE" -> moderate; default -> high; });
    }
}
