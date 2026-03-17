package com.example.capgemini_backend.site;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(20)
public class EmissionFactorSeeder implements CommandLineRunner {

    private static final BigDecimal DEFAULT_WOOD_FACTOR = new BigDecimal("110.0");

    private final EmissionFactorRepository emissionFactorRepository;
    private final boolean enabled;
    private final boolean forceReload;

    public EmissionFactorSeeder(
        EmissionFactorRepository emissionFactorRepository,
        @Value("${app.seed.emission-factors.enabled:true}") boolean enabled,
        @Value("${app.seed.emission-factors.force-reload:false}") boolean forceReload
    ) {
        this.emissionFactorRepository = emissionFactorRepository;
        this.enabled = enabled;
        this.forceReload = forceReload;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        if (forceReload) {
            emissionFactorRepository.deleteAllInBatch();
        } else if (emissionFactorRepository.count() > 0) {
            return;
        }

        Map<MaterialType, BigDecimal> factors = new EnumMap<>(MaterialType.class);
        factors.put(MaterialType.CONCRETE, new BigDecimal("120.0"));
        factors.put(MaterialType.STEEL, new BigDecimal("1900.0"));
        factors.put(MaterialType.GLASS, new BigDecimal("1000.0"));
        factors.put(MaterialType.WOOD, DEFAULT_WOOD_FACTOR);

        factors.forEach((materialType, value) -> {
            EmissionFactor factor = new EmissionFactor();
            factor.setMaterialType(materialType);
            factor.setKgCo2ePerTonne(value.setScale(3, RoundingMode.HALF_UP));
            emissionFactorRepository.save(factor);
        });
    }

}
