package com.example.capgemini_backend.site;

import com.example.capgemini_backend.user.AppUser;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "site")
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 120)
    private String city;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal surfaceM2;

    @Column(nullable = false)
    private Integer parkingSpots;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal annualEnergyMwh;

    @Column(nullable = false)
    private Integer employeeCount;

    @Column(nullable = false)
    private Integer workstationCount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SiteMaterialUsage> materials = new ArrayList<>();

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmissionSnapshot> emissionHistory = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public BigDecimal getSurfaceM2() {
        return surfaceM2;
    }

    public void setSurfaceM2(BigDecimal surfaceM2) {
        this.surfaceM2 = surfaceM2;
    }

    public Integer getParkingSpots() {
        return parkingSpots;
    }

    public void setParkingSpots(Integer parkingSpots) {
        this.parkingSpots = parkingSpots;
    }

    public BigDecimal getAnnualEnergyMwh() {
        return annualEnergyMwh;
    }

    public void setAnnualEnergyMwh(BigDecimal annualEnergyMwh) {
        this.annualEnergyMwh = annualEnergyMwh;
    }

    public Integer getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }

    public Integer getWorkstationCount() {
        return workstationCount;
    }

    public void setWorkstationCount(Integer workstationCount) {
        this.workstationCount = workstationCount;
    }

    public AppUser getOwner() {
        return owner;
    }

    public void setOwner(AppUser owner) {
        this.owner = owner;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<SiteMaterialUsage> getMaterials() {
        return materials;
    }

    public List<EmissionSnapshot> getEmissionHistory() {
        return emissionHistory;
    }
}
