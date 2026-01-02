package com.servicelink.config;

import com.servicelink.model.User;
import com.servicelink.repository.UserRepository;
import com.servicelink.repository.ListingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@Profile("dev")
public class DevDataResetConfig implements CommandLineRunner {
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${app.dev.reset:false}")
    private boolean resetEnabled;

    public DevDataResetConfig(UserRepository userRepository, ListingRepository listingRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!resetEnabled) return;
        userRepository.deleteAll();
        listingRepository.deleteAll();
        User admin = new User();
        admin.setId(1L);
        admin.setName("Admin");
        admin.setUsername("admin");
        admin.setEmail("admin@servicelink.local");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setRoleNames(List.of("ADMIN"));
        admin.setActive(true);
        userRepository.save(admin);
        System.out.println("[DEV RESET] Database wiped and admin user seeded.");
    }
}
