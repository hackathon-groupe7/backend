package com.example.capgemini_backend.site;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "site_material_usage")
public class SiteMaterialUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MaterialType materialType;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal quantityTonnes;

    public UUID getId() {
        return id;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public MaterialType getMaterialType() {
        return materialType;
    }

    public void setMaterialType(MaterialType materialType) {
        this.materialType = materialType;
    }

    public BigDecimal getQuantityTonnes() {
        return quantityTonnes;
    }

    public void setQuantityTonnes(BigDecimal quantityTonnes) {
        this.quantityTonnes = quantityTonnes;
    }
}
