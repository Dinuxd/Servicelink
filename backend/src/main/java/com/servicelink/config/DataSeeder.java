package com.servicelink.config;

import com.servicelink.model.ServiceCategory;
import com.servicelink.model.ServiceListing;
import com.servicelink.model.User;
import com.servicelink.repository.ServiceCategoryRepository;
import com.servicelink.repository.ServiceListingRepository;
import com.servicelink.repository.UserRepository;
import com.servicelink.service.SequenceGeneratorService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initData(UserRepository userRepo,
                               ServiceListingRepository listRepo,
                               ServiceCategoryRepository catRepo,
                               PasswordEncoder encoder,
                               SequenceGeneratorService seq) {
        return args -> {
            if (userRepo.count() == 0) {
                User customer = new User();
                customer.setId(seq.generateSequence("users"));
                customer.setName("Demo Customer");
                customer.setEmail("customer@servicelink.local");
                customer.setPassword(encoder.encode("password"));
                customer.setRoleNames(List.of("ROLE_USER"));
                userRepo.save(customer);

                User provider = new User();
                provider.setId(seq.generateSequence("users"));
                provider.setName("Demo Provider");
                provider.setEmail("provider@servicelink.local");
                provider.setPassword(encoder.encode("password"));
                provider.setRoleNames(List.of("ROLE_PROVIDER"));
                userRepo.save(provider);

                User admin = new User();
                admin.setId(seq.generateSequence("users"));
                admin.setName("Admin");
                admin.setEmail("admin@servicelink.local");
                admin.setPassword(encoder.encode("password"));
                admin.setRoleNames(List.of("ROLE_ADMIN"));
                userRepo.save(admin);

                ServiceCategory cat1 = new ServiceCategory();
                cat1.setId(seq.generateSequence("categories"));
                cat1.setName("Design");
                cat1.setIcon("🎨");
                catRepo.save(cat1);

                ServiceCategory cat2 = new ServiceCategory();
                cat2.setId(seq.generateSequence("categories"));
                cat2.setName("Plumbing");
                cat2.setIcon("🔧");
                catRepo.save(cat2);

                ServiceListing l = new ServiceListing();
                l.setId(seq.generateSequence("listings"));
                l.setTitle("Logo Design");
                l.setDescription("Professional logo design service");
                l.setPrice(new BigDecimal("99.00"));
                l.setOwner(provider);
                l.setCategory(cat1);
                listRepo.save(l);
            }
        };
    }
}