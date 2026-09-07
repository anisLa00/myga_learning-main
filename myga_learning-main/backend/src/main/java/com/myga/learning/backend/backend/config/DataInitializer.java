package com.myga.learning.backend.backend.config;

import com.myga.learning.backend.backend.models.Role;
import com.myga.learning.backend.backend.models.User;
import com.myga.learning.backend.backend.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default administrator on startup if one does not already exist.
 * There is no public signup, so this is how the first ADMIN comes to be.
 *
 * <p>The default credentials are development values (see application.properties)
 * and MUST be overridden via APP_ADMIN_EMAIL / APP_ADMIN_PASSWORD for any real
 * deployment.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminNom;
    private final String adminPrenom;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.admin.email}") String adminEmail,
                           @Value("${app.admin.password}") String adminPassword,
                           @Value("${app.admin.nom}") String adminNom,
                           @Value("${app.admin.prenom}") String adminPrenom) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminNom = adminNom;
        this.adminPrenom = adminPrenom;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }
        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setNom(adminNom);
        admin.setPrenom(adminPrenom);
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        userRepository.save(admin);
        log.warn("Seeded default ADMIN account '{}'. Change its password before any real use.", adminEmail);
    }
}
