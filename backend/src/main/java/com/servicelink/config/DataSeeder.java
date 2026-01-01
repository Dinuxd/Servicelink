package com.servicelink.config;

import com.servicelink.model.ServiceCategory;
import com.servicelink.model.ServiceListing;
import com.servicelink.model.User;
import com.servicelink.model.Booking;
import com.servicelink.model.BookingStatus;
import com.servicelink.repository.ServiceCategoryRepository;
import com.servicelink.repository.ServiceListingRepository;
import com.servicelink.repository.UserRepository;
import com.servicelink.repository.BookingRepository;
import com.servicelink.service.SequenceGeneratorService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initData(UserRepository userRepo,
                               ServiceListingRepository listRepo,
                               ServiceCategoryRepository catRepo,
                               BookingRepository bookingRepo,
                               PasswordEncoder encoder,
                               SequenceGeneratorService seq) {
        return args -> {
            if (userRepo.count() > 0 || catRepo.count() > 0 || listRepo.count() > 0 || bookingRepo.count() > 0) {
                return; // database already has data, skip demo seeding
            }

            // Demo users
            User customer = new User();
            customer.setId(seq.generateSequence("users"));
            customer.setName("Demo Customer");
            customer.setEmail("customer@servicelink.local");
            customer.setPassword(encoder.encode("password"));
            customer.setRoleNames(List.of("ROLE_USER"));
            userRepo.save(customer);

            User provider1 = new User();
            provider1.setId(seq.generateSequence("users"));
            provider1.setName("Demo Provider One");
            provider1.setEmail("provider1@servicelink.local");
            provider1.setPassword(encoder.encode("password"));
            provider1.setRoleNames(List.of("ROLE_PROVIDER"));
            userRepo.save(provider1);

            User provider2 = new User();
            provider2.setId(seq.generateSequence("users"));
            provider2.setName("Demo Provider Two");
            provider2.setEmail("provider2@servicelink.local");
            provider2.setPassword(encoder.encode("password"));
            provider2.setRoleNames(List.of("ROLE_PROVIDER"));
            userRepo.save(provider2);

            User admin = new User();
            admin.setId(seq.generateSequence("users"));
            admin.setName("Demo Admin");
            admin.setEmail("admin@servicelink.local");
            admin.setPassword(encoder.encode("password"));
            admin.setRoleNames(List.of("ROLE_ADMIN"));
            userRepo.save(admin);

            // Categories
            List<String> categoryNames = Arrays.asList(
                    "Home Cleaning",
                    "Home Repair",
                    "Consulting",
                    "Fitness Coaching",
                    "IT Support",
                    "Photography",
                    "Pet Care",
                    "Gardening",
                    "Moving Services",
                    "Tutoring"
            );

            List<ServiceCategory> categories = categoryNames.stream().map(name -> {
                ServiceCategory c = new ServiceCategory();
                c.setId(seq.generateSequence("categories"));
                c.setName(name);
                c.setIcon("");
                return c;
            }).toList();

            catRepo.saveAll(categories);

            // Listings
            ServiceCategory cleaning = categories.get(0);
            ServiceCategory repair = categories.get(1);
            ServiceCategory consulting = categories.get(2);
            ServiceCategory fitness = categories.get(3);
            ServiceCategory it = categories.get(4);

            listRepo.saveAll(List.of(
                    createListing(seq, provider1, cleaning, "Apartment deep clean", "Full home cleaning including kitchen and bathrooms", new BigDecimal("120")),
                    createListing(seq, provider1, cleaning, "Move-out clean", "Detailed move-out cleaning for inspections", new BigDecimal("180")),
                    createListing(seq, provider1, repair, "Kitchen fixture install", "Install or replace kitchen faucets and fixtures", new BigDecimal("90")),
                    createListing(seq, provider2, repair, "Emergency plumbing", "24/7 support for leaks and clogs", new BigDecimal("150")),
                    createListing(seq, provider2, consulting, "Small business consulting", "Strategy sessions for local businesses", new BigDecimal("200")),
                    createListing(seq, provider2, fitness, "In-home personal training", "60-minute strength and conditioning session", new BigDecimal("75")),
                    createListing(seq, provider1, it, "On-site IT support", "Troubleshooting Wi-Fi, printers and devices", new BigDecimal("95")),
                    createListing(seq, provider1, consulting, "Home office setup", "Optimize your space for remote work", new BigDecimal("130")),
                    createListing(seq, provider2, fitness, "Virtual coaching package", "4-week virtual training plan", new BigDecimal("160")),
                    createListing(seq, provider2, cleaning, "Weekly maintenance clean", "Recurring light cleaning service", new BigDecimal("85"))
            ));

            List<ServiceListing> listings = listRepo.findAll();

            // Seed demo bookings
            if (!listings.isEmpty()) {
                ServiceListing aptClean = listings.get(0);
                ServiceListing kitchenFix = listings.get(2);
                ServiceListing consultingListing = listings.get(4);

                Booking b1 = new Booking();
                b1.setId(seq.generateSequence("bookings"));
                b1.setListing(aptClean);
                b1.setCustomer(customer);
                b1.setScheduledAt(LocalDateTime.now().plusHours(2));
                b1.setStatus(BookingStatus.CONFIRMED);
                b1.setAddress("Downtown Loft");
                b1.setNotes("Card on file");

                Booking b2 = new Booking();
                b2.setId(seq.generateSequence("bookings"));
                b2.setListing(kitchenFix);
                b2.setCustomer(customer);
                b2.setScheduledAt(LocalDateTime.now().plusDays(1));
                b2.setStatus(BookingStatus.PENDING);
                b2.setAddress("Suburban Home");
                b2.setNotes("Awaiting payment");

                Booking b3 = new Booking();
                b3.setId(seq.generateSequence("bookings"));
                b3.setListing(consultingListing);
                b3.setCustomer(customer);
                b3.setScheduledAt(LocalDateTime.now().plusWeeks(1));
                b3.setStatus(BookingStatus.CONFIRMED);
                b3.setAddress("Virtual");
                b3.setNotes("Zoom link synced");

                bookingRepo.saveAll(List.of(b1, b2, b3));
            }

            // NOTE: Availability, bookings, and reviews models/repositories
            // are not wired here because their types are not shown in this
            // workspace snapshot. They can be seeded similarly once available.
        };
    }

    private ServiceListing createListing(SequenceGeneratorService seq,
                                         User owner,
                                         ServiceCategory category,
                                         String title,
                                         String description,
                                         BigDecimal price) {
        ServiceListing l = new ServiceListing();
        l.setId(seq.generateSequence("listings"));
        l.setTitle(title);
        l.setDescription(description);
        l.setPrice(price);
        l.setOwner(owner);
        l.setCategory(category);
        return l;
    }
}