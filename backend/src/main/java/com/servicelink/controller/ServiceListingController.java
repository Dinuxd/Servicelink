package com.servicelink.controller;

import com.servicelink.dto.ListingDtos;
import com.servicelink.mapper.ListingMapper;
import com.servicelink.model.ServiceCategory;
import com.servicelink.model.ServiceListing;
import com.servicelink.model.User;
import com.servicelink.repository.ServiceCategoryRepository;
import com.servicelink.repository.ServiceListingRepository;
import com.servicelink.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/listings")
public class ServiceListingController {
    private final ServiceListingRepository repository;
    private final ServiceCategoryRepository categories;
    private final UserService userService;
    private final ListingMapper mapper;

    public ServiceListingController(ServiceListingRepository repository, ServiceCategoryRepository categories, UserService userService, ListingMapper mapper) {
        this.repository = repository;
        this.categories = categories;
        this.userService = userService;
        this.mapper = mapper;
    }

    private ListingDtos.Response toDto(ServiceListing e) {
        return mapper.toDto(e);
    }

    @GetMapping
    public Page<ListingDtos.Response> all(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(required = false) String q,
                                          @RequestParam(required = false) Long categoryId,
                                          @RequestParam(required = false) BigDecimal minPrice,
                                          @RequestParam(required = false) BigDecimal maxPrice,
                                          @RequestParam(required = false) Long ownerId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceListing> pageData = repository.search(q, categoryId, minPrice, maxPrice, ownerId, pageable);
        List<ListingDtos.Response> mapped = pageData.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return new PageImpl<>(Objects.requireNonNull(mapped), pageable, pageData.getTotalElements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingDtos.Response> byId(@PathVariable @NonNull Long id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(toDto(e)))
                .orElseGet(() -> ResponseEntity.status(404).<ListingDtos.Response>build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PROVIDER','ADMIN')")
    public ResponseEntity<ListingDtos.Response> create(@Valid @RequestBody ListingDtos.CreateRequest req, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        User owner = userService.getByEmail(auth.getName());
        if (owner == null) return ResponseEntity.status(401).build();
        ServiceListing listing = new ServiceListing();
        listing.setTitle(req.title);
        listing.setDescription(req.description);
        listing.setPrice(req.price);
        listing.setOwner(owner);
        if (req.categoryId != null) {
            ServiceCategory cat = categories.findById(req.categoryId).orElse(null);
            listing.setCategory(cat);
        }
        ServiceListing saved = repository.save(listing);
        return ResponseEntity.created(Objects.requireNonNull(URI.create("/api/listings/" + saved.getId()))).body(toDto(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROVIDER','ADMIN')")
    public ResponseEntity<ListingDtos.Response> update(@PathVariable @NonNull Long id,
                                                       @Valid @RequestBody ListingDtos.UpdateRequest req,
                                                       Authentication auth) {
        var opt = repository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).build();
        ServiceListing existing = opt.get();
        User owner = userService.getByEmail(auth.getName());
        if (owner == null || !existing.getOwner().getId().equals(owner.getId())) {
            return ResponseEntity.status(403).build();
        }
        existing.setTitle(req.title);
        existing.setDescription(req.description);
        existing.setPrice(req.price);
        if (req.categoryId != null) {
            ServiceCategory cat = categories.findById(req.categoryId).orElse(null);
            existing.setCategory(cat);
        } else {
            existing.setCategory(null);
        }
        return ResponseEntity.ok(toDto(repository.save(existing)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROVIDER','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id, Authentication auth) {
        var opt = repository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        ServiceListing existing = opt.get();
        User owner = userService.getByEmail(auth.getName());
        if (owner == null || !existing.getOwner().getId().equals(owner.getId())) {
            return ResponseEntity.status(403).build();
        }
        repository.delete(existing);
        return ResponseEntity.noContent().build();
    }
}