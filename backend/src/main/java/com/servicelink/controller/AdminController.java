package com.servicelink.controller;

import com.servicelink.dto.UserDtos;
import com.servicelink.dto.ListingDtos;
import com.servicelink.model.ServiceCategory;
import com.servicelink.model.ServiceListing;
import com.servicelink.model.User;
import com.servicelink.repository.ServiceCategoryRepository;
import com.servicelink.repository.ServiceListingRepository;
import com.servicelink.repository.UserRepository;
import com.servicelink.repository.BookingRepository;
import com.servicelink.service.SequenceGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository users;
    private final ServiceCategoryRepository categories;
    private final ServiceListingRepository listings;
    private final BookingRepository bookings;
    private final SequenceGeneratorService seq;

    public AdminController(UserRepository users, ServiceCategoryRepository categories, ServiceListingRepository listings, BookingRepository bookings, SequenceGeneratorService seq) {
        this.users = users;
        this.categories = categories;
        this.listings = listings;
        this.bookings = bookings;
        this.seq = seq;
    }

    private UserDtos.Response toDto(User u) {
        UserDtos.Response r = new UserDtos.Response();
        r.id = u.getId();
        r.name = u.getName();
        r.email = u.getEmail();
        r.active = u.isActive();
        r.roles = u.getRoleNames();
        return r;
    }

    private ListingDtos.Response toListingDto(ServiceListing e) {
        ListingDtos.Response dto = new ListingDtos.Response();
        dto.id = e.getId();
        dto.title = e.getTitle();
        dto.description = e.getDescription();
        dto.price = e.getPrice();
        if (e.getOwner() != null) {
            dto.ownerId = e.getOwner().getId();
            dto.ownerName = e.getOwner().getName();
        }
        if (e.getCategory() != null) {
            dto.categoryId = e.getCategory().getId();
            dto.categoryName = e.getCategory().getName();
        }
        return dto;
    }

    @Operation(summary = "List users with optional role filter")
    @GetMapping("/users")
    public Page<UserDtos.Response> listUsers(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String role) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> p;
        if (role == null || role.isBlank()) {
            p = users.findAll(pageable);
        } else {
            String roleName = "ROLE_" + role.toUpperCase(Locale.ROOT);
            p = users.findByRoleNamesContaining(roleName, pageable);
        }
        List<UserDtos.Response> mapped = p.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return new PageImpl<>(Objects.requireNonNull(mapped), pageable, p.getTotalElements());
    }

    @Operation(summary = "Admin stats")
    @GetMapping("/stats")
    public ResponseEntity<Object> stats() {
        long totalUsers = users.count();
        long activeUsers = users.findAll().stream().filter(User::isActive).count();
        long disabledUsers = totalUsers - activeUsers;
        long totalProviders = users.findAll().stream().filter(u -> u.getRoleNames() != null && u.getRoleNames().contains("ROLE_PROVIDER")).count();
        long totalCustomers = users.findAll().stream().filter(u -> u.getRoleNames() != null && u.getRoleNames().contains("ROLE_USER")).count();
        long totalListings = listings.count();
        long totalCategories = categories.count();
        long totalBookings = bookings.count();

        var body = new java.util.HashMap<String, Object>();
        body.put("totalUsers", totalUsers);
        body.put("activeUsers", activeUsers);
        body.put("disabledUsers", disabledUsers);
        body.put("totalProviders", totalProviders);
        body.put("totalCustomers", totalCustomers);
        body.put("totalListings", totalListings);
        body.put("activeListings", totalListings); // no active flag available
        body.put("totalCategories", totalCategories);
        body.put("totalBookings", totalBookings);
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "Admin list listings")
    @GetMapping("/listings")
    public Page<ListingDtos.Response> adminListings(Pageable pageable) {
        Page<ServiceListing> p = listings.findAll(pageable);
        return p.map(this::toListingDto);
    }

    @Operation(summary = "Admin delete listing")
    @DeleteMapping("/listings/{id}")
    public ResponseEntity<Void> deleteListing(@PathVariable long id) {
        if (!listings.existsById(id)) return ResponseEntity.notFound().build();
        listings.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public record ActiveResponse(Long id, boolean active) {}

    @Operation(summary = "Toggle user active flag")
    @PatchMapping("/users/{id}/toggle-active")
    public ResponseEntity<ActiveResponse> toggleActive(@PathVariable long id) {
        return users.findById(id)
                .map(u -> {
                    u.setActive(!u.isActive());
                    users.save(u);
                    return ResponseEntity.ok(new ActiveResponse(u.getId(), u.isActive()));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Categories CRUD
    @Operation(summary = "List categories")
    @GetMapping("/categories")
    public Page<ServiceCategory> listCategories(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return categories.findAll(PageRequest.of(page, size));
    }

    public static class CategoryRequest {
        @NotBlank public String name;
        public String icon;
    }

    @Operation(summary = "Create category")
    @PostMapping("/categories")
    public ResponseEntity<ServiceCategory> createCategory(@Valid @RequestBody CategoryRequest req) {
        ServiceCategory c = new ServiceCategory();
        c.setId(seq.generateSequence("categories"));
        c.setName(req.name);
        c.setIcon(req.icon);
        ServiceCategory saved = categories.save(c);
        return ResponseEntity.created(Objects.requireNonNull(URI.create("/api/admin/categories/" + saved.getId()))).body(saved);
    }

    @Operation(summary = "Update category")
    @PutMapping("/categories/{id}")
    public ResponseEntity<ServiceCategory> updateCategory(@PathVariable @NonNull Long id,
                                                          @Valid @RequestBody CategoryRequest req) {
        return categories.findById(id)
                .map(existing -> {
                    existing.setName(req.name);
                    existing.setIcon(req.icon);
                    return ResponseEntity.ok(categories.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete category")
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable @NonNull Long id) {
        if (!categories.existsById(id)) return ResponseEntity.notFound().build();
        categories.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
