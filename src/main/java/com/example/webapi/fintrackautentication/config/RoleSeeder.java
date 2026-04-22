package com.example.webapi.fintrackautentication.config;

import com.example.webapi.fintrackautentication.domain.Rol;
import com.example.webapi.fintrackautentication.repository.RolRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RoleSeeder.class);

    private final RolRepository rolRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRole("ROLE_USER");
        seedRole("ROLE_ADMIN");
    }

    private void seedRole(String roleName) {
        try {
            if (!rolRepository.existsByNombre(roleName)) {
                Rol r = new Rol();
                r.setNombre(roleName);
                rolRepository.save(r);
                log.info("Seeded role: {}", roleName);
            }
        } catch (DataIntegrityViolationException e) {
            // another instance may have inserted concurrently; ignore
            log.warn("Role {} may have been created concurrently: {}", roleName, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to seed role {}: {}", roleName, e.getMessage(), e);
        }
    }
}
