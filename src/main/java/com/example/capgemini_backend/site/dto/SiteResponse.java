package com.example.capgemini_backend.site.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SiteResponse(
    UUID id,
    String name,
    String city,
    BigDecimal surfaceM2,
    Integer parkingSpots,
    BigDecimal annualEnergyMwh,
    Integer employeeCount,
    Integer workstationCount,
    EmissionSnapshotResponse latestSnapshot,
    Instant createdAt
) {
}
