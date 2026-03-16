package com.example.capgemini_backend.site.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record EmissionSnapshotResponse(
    BigDecimal constructionKgCo2e,
    BigDecimal operationKgCo2e,
    BigDecimal totalKgCo2e,
    BigDecimal co2PerM2,
    BigDecimal co2PerEmployee,
    Instant computedAt
) {
}
