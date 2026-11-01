package com.healthverse.dto;

import java.time.Instant;

/** accessCode is shown ONCE; only its hash is stored. */
public record SaveResponse(String id, String accessCode, Instant createdAt) {
}
