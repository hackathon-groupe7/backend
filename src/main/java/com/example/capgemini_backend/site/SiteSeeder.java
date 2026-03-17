package com.example.capgemini_backend.site;

import com.example.capgemini_backend.user.AppUser;
import com.example.capgemini_backend.user.UserRepository;
import com.example.capgemini_backend.user.UserRole;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(30)
public class SiteSeeder implements CommandLineRunner {

    private static final long RANDOM_SEED = 20260317L;

    private final UserRepository userRepository;
    private final SiteRepository siteRepository;
    private final EmissionSnapshotRepository snapshotRepository;
    private final CarbonCalculatorService calculatorService;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final boolean forceReload;
    private final String seedUserEmail;
    private final String seedUserPassword;
    private final String csvPath;
    private final int randomCount;

    public SiteSeeder(
        UserRepository userRepository,
        SiteRepository siteRepository,
        EmissionSnapshotRepository snapshotRepository,
        CarbonCalculatorService calculatorService,
        PasswordEncoder passwordEncoder,
        @Value("${app.seed.sites.enabled:false}") boolean enabled,
        @Value("${app.seed.sites.force-reload:false}") boolean forceReload,
        @Value("${app.seed.sites.seed-user-email:demo@capgemini.com}") String seedUserEmail,
        @Value("${app.seed.sites.seed-user-password:DemoPass123!}") String seedUserPassword,
        @Value("${app.seed.sites.csv-path:seed/sites_seed.csv}") String csvPath,
        @Value("${app.seed.sites.random-count:8}") int randomCount
    ) {
        this.userRepository = userRepository;
        this.siteRepository = siteRepository;
        this.snapshotRepository = snapshotRepository;
        this.calculatorService = calculatorService;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.forceReload = forceReload;
        this.seedUserEmail = seedUserEmail;
        this.seedUserPassword = seedUserPassword;
        this.csvPath = csvPath;
        this.randomCount = randomCount;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!enabled) {
            return;
        }

        AppUser owner = userRepository.findByEmailIgnoreCase(seedUserEmail)
            .orElseGet(this::createSeedUser);

        List<Site> existingSites = siteRepository.findAllByOwner(owner);
        if (forceReload && !existingSites.isEmpty()) {
            siteRepository.deleteAll(existingSites);
        } else if (!forceReload && !existingSites.isEmpty()) {
            return;
        }

        List<SiteSeedRow> fixedRows = readSeedRows();
        for (SiteSeedRow row : fixedRows) {
            persistSite(owner, row);
        }

        Random random = new Random(RANDOM_SEED);
        for (int i = 0; i < Math.max(0, randomCount); i++) {
            persistSite(owner, randomRow(i + 1, random));
        }
    }

    private AppUser createSeedUser() {
        AppUser user = new AppUser();
        user.setEmail(seedUserEmail.trim().toLowerCase(Locale.ROOT));
        user.setPasswordHash(passwordEncoder.encode(seedUserPassword));
        user.setRole(UserRole.USER);
        return userRepository.save(user);
    }

    private void persistSite(AppUser owner, SiteSeedRow row) {
        Site site = new Site();
        site.setName(row.name());
        site.setCity(row.city());
        site.setSurfaceM2(row.surfaceM2());
        site.setParkingSpots(row.parkingSpots());
        site.setAnnualEnergyMwh(row.annualEnergyMwh());
        site.setHeatingType(row.heatingType());
        site.setEmployeeCount(row.employeeCount());
        site.setWorkstationCount(row.workstationCount());
        site.setOwner(owner);

        addMaterial(site, MaterialType.CONCRETE, row.concreteTonnes());
        addMaterial(site, MaterialType.STEEL, row.steelTonnes());
        addMaterial(site, MaterialType.GLASS, row.glassTonnes());
        addMaterial(site, MaterialType.WOOD, row.woodTonnes());

        Site saved = siteRepository.save(site);
        EmissionSnapshot snapshot = calculatorService.calculate(saved);
        snapshotRepository.save(snapshot);
    }

    private void addMaterial(Site site, MaterialType materialType, BigDecimal quantityTonnes) {
        SiteMaterialUsage usage = new SiteMaterialUsage();
        usage.setSite(site);
        usage.setMaterialType(materialType);
        usage.setQuantityTonnes(quantityTonnes.max(new BigDecimal("0.001")));
        site.getMaterials().add(usage);
    }

    private List<SiteSeedRow> readSeedRows() throws Exception {
        List<SiteSeedRow> rows = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(csvPath);

        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] parts = trimmed.split(",");
                if (parts.length != 12) {
                    continue;
                }
                rows.add(new SiteSeedRow(
                    parts[0].trim(),
                    parts[1].trim(),
                    new BigDecimal(parts[2].trim()),
                    Integer.parseInt(parts[3].trim()),
                    new BigDecimal(parts[4].trim()),
                    HeatingType.valueOf(parts[5].trim().toUpperCase(Locale.ROOT)),
                    Integer.parseInt(parts[6].trim()),
                    Integer.parseInt(parts[7].trim()),
                    new BigDecimal(parts[8].trim()),
                    new BigDecimal(parts[9].trim()),
                    new BigDecimal(parts[10].trim()),
                    new BigDecimal(parts[11].trim())
                ));
            }
        }

        return rows;
    }

    private SiteSeedRow randomRow(int index, Random random) {
        BigDecimal surfaceM2 = BigDecimal.valueOf(2500 + random.nextInt(15000));
        int parking = 20 + random.nextInt(500);
        BigDecimal annualEnergyMwh = BigDecimal.valueOf(350 + random.nextInt(4800));
        int employees = 80 + random.nextInt(2600);
        int workstations = Math.max(30, (int) Math.round(employees * (0.45 + random.nextDouble() * 0.4)));

        HeatingType[] heatingTypes = HeatingType.values();
        HeatingType heatingType = heatingTypes[random.nextInt(heatingTypes.length)];

        BigDecimal concrete = BigDecimal.valueOf(500 + random.nextInt(12000));
        BigDecimal steel = BigDecimal.valueOf(120 + random.nextInt(3000));
        BigDecimal glass = BigDecimal.valueOf(50 + random.nextInt(1500));
        BigDecimal wood = BigDecimal.valueOf(20 + random.nextInt(900));

        return new SiteSeedRow(
            "Demo Site " + index,
            randomCity(random),
            surfaceM2,
            parking,
            annualEnergyMwh,
            heatingType,
            employees,
            workstations,
            concrete,
            steel,
            glass,
            wood
        );
    }

    private String randomCity(Random random) {
        String[] cities = {"Paris", "Lyon", "Rennes", "Bordeaux", "Toulouse", "Nantes", "Lille", "Montpellier"};
        return cities[random.nextInt(cities.length)];
    }

    private record SiteSeedRow(
        String name,
        String city,
        BigDecimal surfaceM2,
        Integer parkingSpots,
        BigDecimal annualEnergyMwh,
        HeatingType heatingType,
        Integer employeeCount,
        Integer workstationCount,
        BigDecimal concreteTonnes,
        BigDecimal steelTonnes,
        BigDecimal glassTonnes,
        BigDecimal woodTonnes
    ) {
    }
}
