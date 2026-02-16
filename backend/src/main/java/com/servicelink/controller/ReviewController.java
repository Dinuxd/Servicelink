package com.servicelink.controller;

import com.servicelink.dto.ReviewDtos;
import com.servicelink.model.Booking;
import com.servicelink.model.BookingStatus;
import com.servicelink.model.Review;
import com.servicelink.model.User;
import com.servicelink.repository.BookingRepository;
import com.servicelink.repository.ReviewRepository;
import com.servicelink.service.UserService;
import com.servicelink.service.SequenceGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ReviewController {
    private final ReviewRepository reviews;
    private final BookingRepository bookings;
    private final UserService users;
    private final SequenceGeneratorService seq;

    public ReviewController(ReviewRepository reviews, BookingRepository bookings, UserService users, SequenceGeneratorService seq) {
        this.reviews = reviews;
        this.bookings = bookings;
        this.users = users;
        this.seq = seq;
    }

    private ReviewDtos.Response toDto(Review r) {
        ReviewDtos.Response out = new ReviewDtos.Response();
        out.id = r.getId();
        out.bookingId = r.getBooking().getId();
        out.rating = r.getRating();
        out.content = r.getContent();
        out.authorId = r.getBooking().getCustomer().getId();
        out.authorName = r.getBooking().getCustomer().getName();
        out.createdAt = r.getCreatedAt();
        return out;
    }

    @Operation(summary = "Create a review for a completed booking")
    @PostMapping("/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewDtos.Response> create(@Valid @RequestBody ReviewDtos.CreateRequest req, Authentication auth) {
        User me = users.getByEmail(auth.getName());
        Long bookingId = Objects.requireNonNull(req.bookingId);
        Booking b = bookings.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (!b.getCustomer().getId().equals(me.getId())) return ResponseEntity.status(403).build();
        if (b.getStatus() != BookingStatus.COMPLETED) return ResponseEntity.badRequest().build();
        if (reviews.existsByBooking_Id(bookingId)) return ResponseEntity.badRequest().build();
        Review r = new Review();
        r.setId(seq.generateSequence("reviews"));
        r.setBooking(b);
        r.setRating(req.rating);
        r.setContent(req.content);
        return ResponseEntity.status(201).body(toDto(reviews.save(r)));
    }

    @Operation(summary = "Get reviews for a listing")
    @GetMapping("/listings/{id}/reviews")
    public List<ReviewDtos.Response> forListing(@PathVariable @NonNull Long id) {
        return reviews.findByBooking_Listing_Id(id).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Operation(summary = "Can current user review this listing? Returns eligible bookingId if yes")
    @GetMapping("/reviews/eligibility")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> eligibility(@RequestParam Long listingId, Authentication auth) {
        User me = users.getByEmail(auth.getName());
        List<Booking> completed = bookings.findByCustomer_IdAndListing_Id(me.getId(), listingId).stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .toList();
        Optional<Booking> first = completed.stream()
                .filter(b -> !reviews.existsByBooking_Id(b.getId()))
                .findFirst();
        boolean eligible = first.isPresent();
        Long bookingId = first.map(Booking::getId).orElse(null);
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("eligible", eligible);
        body.put("bookingId", bookingId); // may be null when not eligible
        return ResponseEntity.ok(body);
    }
}
