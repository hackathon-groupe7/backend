package com.example.capgemini_backend.site;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmissionSnapshotRepository extends JpaRepository<EmissionSnapshot, UUID> {
    List<EmissionSnapshot> findBySiteIdOrderByComputedAtDesc(UUID siteId);
}
