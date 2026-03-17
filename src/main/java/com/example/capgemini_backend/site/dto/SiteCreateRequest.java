package com.example.capgemini_backend.site.dto;

import com.example.capgemini_backend.site.HeatingType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record SiteCreateRequest(
    @NotBlank @Size(max = 120) String name,
    @Size(max = 120) String city,
    @NotNull @DecimalMin(value = "1.0", inclusive = true) BigDecimal surfaceM2,
    @NotNull @Min(0) Integer parkingSpots,
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal annualEnergyMwh,
    @NotNull HeatingType heatingType,
    @NotNull @Min(1) Integer employeeCount,
    @NotNull @Min(0) Integer workstationCount,
    @NotEmpty List<@Valid MaterialUsageRequest> materials
) {
}
