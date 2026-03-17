package com.example.capgemini_backend.site;

public enum HeatingType {
    GAS("chauffagegaz"),
    FUEL_OIL("chauffagefioul"),
    ELECTRIC("chauffageelectrique"),
    HEAT_PUMP("pompeachaleur"),
    PELLET_STOVE("poeleagranule"),
    WOOD_STOVE("poeleabois"),
    DISTRICT_HEATING("reseaudechaleur"),
    PELLET_BOILER("chaudiereagranule"),
    WOOD_BOILER("chaudiereabois");

    private final String impactCo2Slug;

    HeatingType(String impactCo2Slug) {
        this.impactCo2Slug = impactCo2Slug;
    }

    public String getImpactCo2Slug() {
        return impactCo2Slug;
    }
}
