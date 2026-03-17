package com.example.capgemini_backend.site;

import com.example.capgemini_backend.site.dto.EmissionSnapshotResponse;
import com.example.capgemini_backend.site.dto.SiteComparisonResponse;
import com.example.capgemini_backend.site.dto.SiteCreateRequest;
import com.example.capgemini_backend.site.dto.SiteResponse;
import com.example.capgemini_backend.user.AppUser;
import com.example.capgemini_backend.user.UserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SiteService {

    private final UserRepository userRepository;
    private final SiteRepository siteRepository;
    private final EmissionSnapshotRepository snapshotRepository;
    private final CarbonCalculatorService calculatorService;
    private final SiteMapper siteMapper;

    public SiteService(
        UserRepository userRepository,
        SiteRepository siteRepository,
        EmissionSnapshotRepository snapshotRepository,
        CarbonCalculatorService calculatorService,
        SiteMapper siteMapper
    ) {
        this.userRepository = userRepository;
        this.siteRepository = siteRepository;
        this.snapshotRepository = snapshotRepository;
        this.calculatorService = calculatorService;
        this.siteMapper = siteMapper;
    }

    @Transactional
    public SiteResponse createSite(SiteCreateRequest request) {
        AppUser currentUser = getCurrentUser();

        Site site = new Site();
        site.setName(request.name());
        site.setCity(request.city());
        site.setSurfaceM2(request.surfaceM2());
        site.setParkingSpots(request.parkingSpots());
        site.setAnnualEnergyMwh(request.annualEnergyMwh());
        site.setHeatingType(request.heatingType());
        site.setEmployeeCount(request.employeeCount());
        site.setWorkstationCount(request.workstationCount());
        site.setOwner(currentUser);

        request.materials().forEach(materialReq -> {
            SiteMaterialUsage material = new SiteMaterialUsage();
            material.setSite(site);
            material.setMaterialType(materialReq.materialType());
            material.setQuantityTonnes(materialReq.quantityTonnes());
            site.getMaterials().add(material);
        });

        Site savedSite = siteRepository.save(site);
        EmissionSnapshot snapshot = calculatorService.calculate(savedSite);
        snapshotRepository.save(snapshot);
        savedSite.getEmissionHistory().add(snapshot);

        return siteMapper.toResponse(savedSite);
    }

    @Transactional(readOnly = true)
    public List<SiteResponse> listMySites() {
        return siteRepository.findAllByOwner(getCurrentUser()).stream()
            .map(siteMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public SiteResponse getSite(UUID id) {
        Site site = siteRepository.findByIdAndOwner(id, getCurrentUser())
            .orElseThrow(() -> new IllegalArgumentException("Site not found"));
        return siteMapper.toResponse(site);
    }

    @Transactional(readOnly = true)
    public List<EmissionSnapshotResponse> getHistory(UUID siteId) {
        Site site = siteRepository.findByIdAndOwner(siteId, getCurrentUser())
            .orElseThrow(() -> new IllegalArgumentException("Site not found"));

        return snapshotRepository.findBySiteIdOrderByComputedAtDesc(site.getId()).stream()
            .map(siteMapper::toSnapshotResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public SiteComparisonResponse compare(List<UUID> siteIds) {
        if (siteIds == null || siteIds.size() < 2) {
            throw new IllegalArgumentException("At least 2 site ids are required for comparison");
        }

        AppUser currentUser = getCurrentUser();
        List<SiteResponse> sites = siteIds.stream()
            .map(id -> siteRepository.findByIdAndOwner(id, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Site not found: " + id)))
            .sorted(Comparator.comparing(Site::getName))
            .map(siteMapper::toResponse)
            .toList();

        return new SiteComparisonResponse(sites);
    }

    private AppUser getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
    }
}
