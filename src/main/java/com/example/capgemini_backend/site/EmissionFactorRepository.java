package com.example.capgemini_backend.site;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmissionFactorRepository extends JpaRepository<EmissionFactor, UUID> {
    Optional<EmissionFactor> findByMaterialType(MaterialType materialType);
}
