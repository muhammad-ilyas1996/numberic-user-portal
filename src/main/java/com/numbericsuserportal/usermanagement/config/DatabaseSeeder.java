package com.numbericsuserportal.usermanagement.config;

import com.numbericsuserportal.usermanagement.domain.PortalType;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.repo.PortalTypeRepository;
import com.numbericsuserportal.usermanagement.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PortalTypeRepository portalTypeRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, PortalTypeRepository portalTypeRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.portalTypeRepository = portalTypeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        seedSuperAdmin();
    }

    private void seedSuperAdmin() {
        if (userRepository.findByEmail("eman@numbrics.ai").isEmpty()) {
            PortalType portalType = portalTypeRepository.findByPortalName("ADMIN_PORTAL").orElseGet(() -> {
                PortalType newPortalType = new PortalType();
                newPortalType.setPortalName("ADMIN_PORTAL");
                newPortalType.setDisplayName("Admin Portal");
                return portalTypeRepository.save(newPortalType);
            });

            User superAdmin = new User();
            superAdmin.setUsername("eman@numbrics.ai");
            superAdmin.setEmail("eman@numbrics.ai");
            superAdmin.setPassword(passwordEncoder.encode("Desire$Money26"));
            superAdmin.setUserType(User.UserType.admin);
            superAdmin.setFirstName("Super");
            superAdmin.setLastName("Admin");
            superAdmin.setIsActive(true);
            superAdmin.setIsDeleted(false);
            superAdmin.setCreatedAt(LocalDateTime.now());
            superAdmin.setUpdatedAt(LocalDateTime.now());
            superAdmin.setPortalType(portalType);
            
            userRepository.save(superAdmin);
            System.out.println("Super admin seeded: eman@numbrics.ai");
        }
    }
}
