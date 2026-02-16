package com.servicelink.controller;

import com.servicelink.dto.BookingDtos;
import com.servicelink.model.Booking;
import com.servicelink.model.User;
import com.servicelink.repository.BookingRepository;
import com.servicelink.service.BookingService;
import com.servicelink.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
// Support both canonical /api/bookings and a fallback /bookings in case a context-path adds /api
@RequestMapping({"/api/bookings", "/bookings"})
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final BookingRepository bookingRepository;

    public BookingController(BookingService bookingService, UserService userService, BookingRepository bookingRepository) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.bookingRepository = bookingRepository;
    }

    private BookingDtos.Response toDto(Booking b) {
        BookingDtos.Response r = new BookingDtos.Response();
        r.id = b.getId();
        r.listingId = b.getListing() != null ? b.getListing().getId() : null;
        r.customerId = b.getCustomer() != null ? b.getCustomer().getId() : null;
        r.providerId = b.getProviderId();
        r.slotId = b.getSlotId();
        r.scheduledAt = b.getScheduledAt();
        r.status = b.getStatus();
        r.paymentStatus = b.getPaymentStatus();
        r.paymentRef = b.getPaymentRef();
        r.paidAt = b.getPaidAt();
        r.address = b.getAddress();
        r.notes = b.getNotes();
        r.listingTitle = b.getListing() != null ? b.getListing().getTitle() : null;
        if (b.getListing() != null && b.getListing().getOwner() != null) {
            r.providerName = b.getListing().getOwner().getName();
        }
        r.customerName = b.getCustomer() != null ? b.getCustomer().getName() : null;
        r.price = b.getListing() != null ? b.getListing().getPrice() : null;
        r.createdAt = b.getCreatedAt();
        return r;
    }

    public record BookingSummary(
            Long id,
            String categoryName,
            String serviceTitle,
            String clientName,
            String providerName,
            String status,
            String scheduledLabel
    ) {
    }

    @Operation(summary = "Create a booking")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDtos.Response> create(@Valid @RequestBody BookingDtos.CreateRequest req, Authentication auth) {
        User me = userService.getByEmail(auth.getName());
        Booking b = bookingService.create(me, req);
        return ResponseEntity.ok(toDto(b));
    }

    @Operation(summary = "Dummy pay a booking (marks as paid)")
    @PostMapping("/{id}/pay")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDtos.Response> pay(@PathVariable Long id, Authentication auth) {
        User me = userService.getByEmail(auth.getName());
        Booking b = bookingService.dummyPay(id, me);
        return ResponseEntity.ok(toDto(b));
    }

    @Operation(summary = "Provider earnings totals")
    @GetMapping("/earnings/provider")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDtos.EarningsResponse> providerEarnings(Authentication auth) {
        User me = userService.getByEmail(auth.getName());
        BookingDtos.EarningsResponse resp = bookingService.providerEarnings(me);
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "List my bookings as customer or provider")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<BookingDtos.Response> myBookings(@RequestParam(defaultValue = "customer") String as,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    Authentication auth) {
        User me = userService.getByEmail(auth.getName());
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> p = ("provider".equalsIgnoreCase(as)) ? bookingService.forProvider(me, pageable) : bookingService.forCustomer(me, pageable);
        List<BookingDtos.Response> mapped = p.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return new PageImpl<>(Objects.requireNonNull(mapped), pageable, p.getTotalElements());
    }

    @Operation(summary = "Change booking status")
    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDtos.Response> changeStatus(@PathVariable Long id, @Valid @RequestBody BookingDtos.StatusRequest req, Authentication auth) {
        User me = userService.getByEmail(auth.getName());
        Booking b = bookingService.changeStatus(id, req.status, me);
        return ResponseEntity.ok(toDto(b));
    }

    @Operation(summary = "Reschedule booking")
    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDtos.Response> reschedule(@PathVariable Long id, @Valid @RequestBody BookingDtos.RescheduleRequest req, Authentication auth) {
        User me = userService.getByEmail(auth.getName());
        Booking b = bookingService.reschedule(id, req.scheduledAt, me);
        return ResponseEntity.ok(toDto(b));
    }

    @Operation(summary = "Get a booking by id (participants only)")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDtos.Response> getById(@PathVariable @NonNull Long id, Authentication auth) {
        User me = userService.getByEmail(auth.getName());
        return ResponseEntity.ok(toDto(bookingService.getForParticipant(id, me)));
    }

    @Operation(summary = "Get booking summaries for homepage")
    @GetMapping("/summary")
        public List<BookingSummary> summary(Authentication auth) {
        User me = auth == null ? null : userService.getByEmail(auth.getName());
        boolean isAdmin = me != null && me.getRoleNames() != null && me.getRoleNames().contains("ROLE_ADMIN");
        List<Booking> source = bookingRepository.findAll();
        if (!isAdmin && me != null) {
            source = source.stream().filter(b ->
                (b.getCustomer() != null && b.getCustomer().getId().equals(me.getId())) ||
                    (b.getProviderId() != null && b.getProviderId().equals(me.getId()))
            ).toList();
        }
        List<Booking> latest = source.stream()
            .sorted((a, b) -> b.getScheduledAt().compareTo(a.getScheduledAt()))
            .limit(5)
            .toList();

        return latest.stream().map(b -> new BookingSummary(
            b.getId(),
            b.getListing() != null && b.getListing().getCategory() != null
                ? b.getListing().getCategory().getName()
                : null,
            b.getListing() != null ? b.getListing().getTitle() : null,
            b.getCustomer() != null ? b.getCustomer().getName() : null,
            b.getListing() != null && b.getListing().getOwner() != null
                ? b.getListing().getOwner().getName()
                : null,
            b.getStatus() != null ? b.getStatus().name() : null,
            b.getScheduledAt() != null ? b.getScheduledAt().toString() : null
        )).collect(Collectors.toList());
        }
}
