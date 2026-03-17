package com.example.capgemini_backend.site;

import com.example.capgemini_backend.site.dto.EmissionSnapshotResponse;
import com.example.capgemini_backend.site.dto.SiteResponse;
import java.util.Comparator;
import org.springframework.stereotype.Component;

@Component
public class SiteMapper {

    public SiteResponse toResponse(Site site) {
        EmissionSnapshot latest = site.getEmissionHistory().stream()
            .max(Comparator.comparing(EmissionSnapshot::getComputedAt))
            .orElse(null);

        return new SiteResponse(
            site.getId(),
            site.getName(),
            site.getCity(),
            site.getSurfaceM2(),
            site.getParkingSpots(),
            site.getAnnualEnergyMwh(),
            site.getHeatingType(),
            site.getEmployeeCount(),
            site.getWorkstationCount(),
            latest == null ? null : toSnapshotResponse(latest),
            site.getCreatedAt()
        );
    }

    public EmissionSnapshotResponse toSnapshotResponse(EmissionSnapshot snapshot) {
        return new EmissionSnapshotResponse(
            snapshot.getConstructionKgCo2e(),
            snapshot.getOperationKgCo2e(),
            snapshot.getTotalKgCo2e(),
            snapshot.getCo2PerM2(),
            snapshot.getCo2PerEmployee(),
            snapshot.getComputedAt()
        );
    }
}
