package com.example.capgemini_backend.site;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "emission_snapshot")
public class EmissionSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(nullable = false, precision = 16, scale = 3)
    private BigDecimal constructionKgCo2e;

    @Column(nullable = false, precision = 16, scale = 3)
    private BigDecimal operationKgCo2e;

    @Column(nullable = false, precision = 16, scale = 3)
    private BigDecimal totalKgCo2e;

    @Column(nullable = false, precision = 16, scale = 6)
    private BigDecimal co2PerM2;

    @Column(nullable = false, precision = 16, scale = 6)
    private BigDecimal co2PerEmployee;

    @Column(nullable = false, updatable = false)
    private Instant computedAt;

    @PrePersist
    void onCreate() {
        computedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public BigDecimal getConstructionKgCo2e() {
        return constructionKgCo2e;
    }

    public void setConstructionKgCo2e(BigDecimal constructionKgCo2e) {
        this.constructionKgCo2e = constructionKgCo2e;
    }

    public BigDecimal getOperationKgCo2e() {
        return operationKgCo2e;
    }

    public void setOperationKgCo2e(BigDecimal operationKgCo2e) {
        this.operationKgCo2e = operationKgCo2e;
    }

    public BigDecimal getTotalKgCo2e() {
        return totalKgCo2e;
    }

    public void setTotalKgCo2e(BigDecimal totalKgCo2e) {
        this.totalKgCo2e = totalKgCo2e;
    }

    public BigDecimal getCo2PerM2() {
        return co2PerM2;
    }

    public void setCo2PerM2(BigDecimal co2PerM2) {
        this.co2PerM2 = co2PerM2;
    }

    public BigDecimal getCo2PerEmployee() {
        return co2PerEmployee;
    }

    public void setCo2PerEmployee(BigDecimal co2PerEmployee) {
        this.co2PerEmployee = co2PerEmployee;
    }

    public Instant getComputedAt() {
        return computedAt;
    }
}
