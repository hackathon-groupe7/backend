package com.example.capgemini_backend.site.dto;

import java.util.List;

public record SiteComparisonResponse(
    List<SiteResponse> sites
) {
}
