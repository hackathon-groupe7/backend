package com.example.capgemini_backend.site.dto;

import com.example.capgemini_backend.site.MaterialType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MaterialUsageRequest(
    @NotNull MaterialType materialType,
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantityTonnes
) {
}
