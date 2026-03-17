package com.example.capgemini_backend.site;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "emission_factor")
public class EmissionFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private MaterialType materialType;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal kgCo2ePerTonne;

    public UUID getId() {
        return id;
    }

    public MaterialType getMaterialType() {
        return materialType;
    }

    public void setMaterialType(MaterialType materialType) {
        this.materialType = materialType;
    }

    public BigDecimal getKgCo2ePerTonne() {
        return kgCo2ePerTonne;
    }

    public void setKgCo2ePerTonne(BigDecimal kgCo2ePerTonne) {
        this.kgCo2ePerTonne = kgCo2ePerTonne;
    }
}
