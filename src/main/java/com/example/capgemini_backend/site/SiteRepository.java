package com.example.capgemini_backend.site;

import com.example.capgemini_backend.user.AppUser;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteRepository extends JpaRepository<Site, UUID> {

    @EntityGraph(attributePaths = {"materials", "emissionHistory"})
    Optional<Site> findByIdAndOwner(UUID id, AppUser owner);

    @EntityGraph(attributePaths = {"emissionHistory"})
    List<Site> findAllByOwner(AppUser owner);
}
