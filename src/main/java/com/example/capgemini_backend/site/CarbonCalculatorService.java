package com.example.capgemini_backend.site;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CarbonCalculatorService {

    // Fallback factors used when DB or API data is unavailable.
    private static final Map<MaterialType, BigDecimal> DEFAULT_MATERIAL_FACTORS = new EnumMap<>(MaterialType.class);
    private final EmissionFactorRepository emissionFactorRepository;
    private final ImpactCo2Service impactCo2Service;

    static {
        DEFAULT_MATERIAL_FACTORS.put(MaterialType.CONCRETE, new BigDecimal("120.0"));
        DEFAULT_MATERIAL_FACTORS.put(MaterialType.STEEL, new BigDecimal("1900.0"));
        DEFAULT_MATERIAL_FACTORS.put(MaterialType.GLASS, new BigDecimal("1000.0"));
        DEFAULT_MATERIAL_FACTORS.put(MaterialType.WOOD, new BigDecimal("110.0"));
    }

    public CarbonCalculatorService(
        EmissionFactorRepository emissionFactorRepository,
        ImpactCo2Service impactCo2Service
    ) {
        this.emissionFactorRepository = emissionFactorRepository;
        this.impactCo2Service = impactCo2Service;
    }

    public EmissionSnapshot calculate(Site site) {
        BigDecimal construction = site.getMaterials().stream()
            .map(m -> m.getQuantityTonnes().multiply(getFactor(m.getMaterialType())))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(3, RoundingMode.HALF_UP);

        BigDecimal operation = site.getAnnualEnergyMwh()
            .multiply(impactCo2Service.getOperationFactorKgPerMwh(site.getHeatingType()))
            .setScale(3, RoundingMode.HALF_UP);

        BigDecimal total = construction.add(operation).setScale(3, RoundingMode.HALF_UP);
        BigDecimal perM2 = safeDivide(total, site.getSurfaceM2());
        BigDecimal perEmployee = safeDivide(total, BigDecimal.valueOf(site.getEmployeeCount()));

        EmissionSnapshot snapshot = new EmissionSnapshot();
        snapshot.setSite(site);
        snapshot.setConstructionKgCo2e(construction);
        snapshot.setOperationKgCo2e(operation);
        snapshot.setTotalKgCo2e(total);
        snapshot.setCo2PerM2(perM2);
        snapshot.setCo2PerEmployee(perEmployee);
        return snapshot;
    }

    private BigDecimal getFactor(MaterialType materialType) {
        return emissionFactorRepository.findByMaterialType(materialType)
            .map(EmissionFactor::getKgCo2ePerTonne)
            .orElseGet(() -> DEFAULT_MATERIAL_FACTORS.getOrDefault(materialType, BigDecimal.ZERO));
    }

    private BigDecimal safeDivide(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);
        }
        return numerator.divide(denominator, 6, RoundingMode.HALF_UP);
    }
}
