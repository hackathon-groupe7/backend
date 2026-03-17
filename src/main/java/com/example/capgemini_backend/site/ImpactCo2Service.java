package com.example.capgemini_backend.site;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ImpactCo2Service {

    private static final Logger logger = LoggerFactory.getLogger(ImpactCo2Service.class);
    private static final BigDecimal FALLBACK_ENERGY_FACTOR_KG_PER_MWH = new BigDecimal("56.0");

    private final RestClient restClient;
    private final String apiKey;
    private final int referenceSurfaceM2;
    private final BigDecimal referenceAnnualConsumptionMwh;
    private final AtomicReference<Map<HeatingType, BigDecimal>> cachedFactorsByHeatingType = new AtomicReference<>();

    public ImpactCo2Service(
        RestClient.Builder restClientBuilder,
        @Value("${app.impact-co2.base-url:https://impactco2.fr/api/v1}") String baseUrl,
        @Value("${app.impact-co2.api-key:}") String apiKey,
        @Value("${app.impact-co2.reference-surface-m2:100}") int referenceSurfaceM2,
        @Value("${app.impact-co2.reference-annual-consumption-mwh:10.0}") BigDecimal referenceAnnualConsumptionMwh
    ) {
        this.restClient = restClientBuilder.baseUrl(normalizeQuoted(baseUrl)).build();
        this.apiKey = normalizeQuoted(apiKey);
        this.referenceSurfaceM2 = referenceSurfaceM2;
        this.referenceAnnualConsumptionMwh = referenceAnnualConsumptionMwh;
    }

    public BigDecimal getOperationFactorKgPerMwh(HeatingType heatingType) {
        HeatingType effectiveType = heatingType == null ? HeatingType.ELECTRIC : heatingType;
        Map<HeatingType, BigDecimal> cachedValues = cachedFactorsByHeatingType.get();
        if (cachedValues != null) {
            return cachedValues.getOrDefault(effectiveType, FALLBACK_ENERGY_FACTOR_KG_PER_MWH);
        }

        Map<HeatingType, BigDecimal> resolved = fetchFactorsFromImpactCo2()
            .orElseGet(() -> buildFallbackFactorMap(FALLBACK_ENERGY_FACTOR_KG_PER_MWH));
        cachedFactorsByHeatingType.compareAndSet(null, resolved);
        return cachedFactorsByHeatingType.get().getOrDefault(effectiveType, FALLBACK_ENERGY_FACTOR_KG_PER_MWH);
    }

    private Optional<Map<HeatingType, BigDecimal>> fetchFactorsFromImpactCo2() {
        if (referenceAnnualConsumptionMwh == null || referenceAnnualConsumptionMwh.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Invalid reference annual consumption configured for Impact CO2 integration.");
            return Optional.empty();
        }

        try {
            Map<?, ?> payload = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/chauffage")
                    .queryParam("m2", referenceSurfaceM2)
                    .queryParam("language", "fr")
                    .build()
                )
                .headers(headers -> {
                    if (apiKey != null && !apiKey.isBlank()) {
                        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim());
                    }
                })
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Map.class);

            if (payload == null || !(payload.get("data") instanceof List<?> data)) {
                return Optional.empty();
            }

            Map<String, BigDecimal> factorBySlug = new java.util.HashMap<>();
            for (Object entry : data) {
                if (!(entry instanceof Map<?, ?> item)) {
                    continue;
                }
                Object slugValue = item.get("slug");
                Object ecvValue = item.get("ecv");
                if (!(slugValue instanceof String slug) || !(ecvValue instanceof Number ecvNumber)) {
                    continue;
                }
                BigDecimal ecvKgCo2e = BigDecimal.valueOf(ecvNumber.doubleValue());
                BigDecimal factor = ecvKgCo2e.divide(referenceAnnualConsumptionMwh, 6, RoundingMode.HALF_UP);
                factorBySlug.put(slug, factor);
            }

            Map<HeatingType, BigDecimal> resolved = new EnumMap<>(HeatingType.class);
            for (HeatingType type : HeatingType.values()) {
                BigDecimal factor = factorBySlug.getOrDefault(type.getImpactCo2Slug(), FALLBACK_ENERGY_FACTOR_KG_PER_MWH);
                resolved.put(type, factor);
            }
            return Optional.of(resolved);
        } catch (Exception ex) {
            logger.warn("Unable to fetch operation factor from Impact CO2 API. Falling back to local value.", ex);
        }

        return Optional.empty();
    }

    private Map<HeatingType, BigDecimal> buildFallbackFactorMap(BigDecimal value) {
        Map<HeatingType, BigDecimal> fallback = new EnumMap<>(HeatingType.class);
        for (HeatingType type : HeatingType.values()) {
            fallback.put(type, value);
        }
        return fallback;
    }

    private String normalizeQuoted(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
            || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            return trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return trimmed;
    }
}
