package com.example.capgemini_backend.site;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CarbonCalculatorService {

    // Approximate factors, intended as replaceable defaults (kgCO2e per tonne).
    private static final Map<MaterialType, BigDecimal> MATERIAL_FACTORS = new EnumMap<>(MaterialType.class);
    private static final BigDecimal ENERGY_FACTOR_KG_PER_MWH = new BigDecimal("56.0");

    static {
        MATERIAL_FACTORS.put(MaterialType.CONCRETE, new BigDecimal("120.0"));
        MATERIAL_FACTORS.put(MaterialType.STEEL, new BigDecimal("1900.0"));
        MATERIAL_FACTORS.put(MaterialType.GLASS, new BigDecimal("1000.0"));
        MATERIAL_FACTORS.put(MaterialType.WOOD, new BigDecimal("110.0"));
    }

    public EmissionSnapshot calculate(Site site) {
        BigDecimal construction = site.getMaterials().stream()
            .map(m -> m.getQuantityTonnes().multiply(getFactor(m.getMaterialType())))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(3, RoundingMode.HALF_UP);

        BigDecimal operation = site.getAnnualEnergyMwh()
            .multiply(ENERGY_FACTOR_KG_PER_MWH)
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
        return MATERIAL_FACTORS.getOrDefault(materialType, BigDecimal.ZERO);
    }

    private BigDecimal safeDivide(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);
        }
        return numerator.divide(denominator, 6, RoundingMode.HALF_UP);
    }
}
