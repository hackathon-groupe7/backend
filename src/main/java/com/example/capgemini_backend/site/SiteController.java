package com.example.capgemini_backend.site;

import com.example.capgemini_backend.site.dto.EmissionSnapshotResponse;
import com.example.capgemini_backend.site.dto.SiteComparisonResponse;
import com.example.capgemini_backend.site.dto.SiteCreateRequest;
import com.example.capgemini_backend.site.dto.SiteResponse;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sites")
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @PostMapping
    SiteResponse create(@Valid @RequestBody SiteCreateRequest request) {
        return siteService.createSite(request);
    }

    @GetMapping
    List<SiteResponse> listMine() {
        return siteService.listMySites();
    }

    @GetMapping("/{id}")
    SiteResponse getOne(@PathVariable UUID id) {
        return siteService.getSite(id);
    }

    @GetMapping("/{id}/history")
    List<EmissionSnapshotResponse> history(@PathVariable UUID id) {
        return siteService.getHistory(id);
    }

    @GetMapping("/compare")
    SiteComparisonResponse compare(@RequestParam List<UUID> ids) {
        return siteService.compare(ids);
    }

    @GetMapping("/heating-types")
    List<HeatingType> heatingTypes() {
        return Arrays.asList(HeatingType.values());
    }
}
