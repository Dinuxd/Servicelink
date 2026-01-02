package com.servicelink.controller;

import com.servicelink.dto.MessageDtos;
import com.servicelink.model.Booking;
import com.servicelink.model.Message;
import com.servicelink.model.User;
import com.servicelink.repository.BookingRepository;
import com.servicelink.repository.MessageRepository;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings/{bookingId}/messages")
public class MessagesController {

    private final MessageRepository messages;
    private final BookingRepository bookings;
    private final UserService users;
    private final SequenceGeneratorService seq;

    public MessagesController(MessageRepository messages, BookingRepository bookings, UserService users, SequenceGeneratorService seq) {
        this.messages = messages;
        this.bookings = bookings;
        this.users = users;
        this.seq = seq;
    }

    private boolean isParticipant(User me, Booking b) {
        return b.getCustomer().getId().equals(me.getId()) || b.getListing().getOwner().getId().equals(me.getId());
    }

    private MessageDtos.Response toDto(Message m) {
        MessageDtos.Response r = new MessageDtos.Response();
        r.id = m.getId();
        r.bookingId = m.getBooking().getId();
        r.senderId = m.getSender().getId();
        r.content = m.getContent();
        return r;
    }

    @Operation(summary = "Get messages for a booking (participants only)")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MessageDtos.Response>> list(@PathVariable @NonNull Long bookingId, Authentication auth) {
        User me = users.getByEmail(auth.getName());
        Booking b = bookings.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (!isParticipant(me, b)) return ResponseEntity.status(403).build();
        List<MessageDtos.Response> out = messages.findByBooking_IdOrderBySentAtAsc(bookingId)
                .stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    @Operation(summary = "Post a message to a booking (participants only)")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageDtos.Response> post(@PathVariable @NonNull Long bookingId,
                                                     @Valid @RequestBody MessageDtos.CreateRequest req,
                                                     Authentication auth) {
        User me = users.getByEmail(auth.getName());
        Booking b = bookings.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (!isParticipant(me, b)) return ResponseEntity.status(403).build();
        Message m = new Message();
        m.setId(seq.generateSequence("messages"));
        m.setBooking(b);
        m.setSender(me);
        m.setContent(req.content);
        Message saved = messages.save(m);
        return ResponseEntity.ok(toDto(saved));
    }
}
