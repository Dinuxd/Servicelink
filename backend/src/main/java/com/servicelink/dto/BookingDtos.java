package com.servicelink.dto;

import com.servicelink.model.BookingStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class BookingDtos {
    public static class CreateRequest {
        @NotNull
        public Long listingId;
        @NotNull @Future
        public LocalDateTime scheduledAt;
        public String address;
        public String notes;
    }
    public static class StatusRequest {
        @NotNull
        public BookingStatus status;
    }
    public static class RescheduleRequest {
        @NotNull @Future
        public LocalDateTime scheduledAt;
    }
    public static class Response {
        public Long id;
        public Long listingId;
        public Long customerId;
        public LocalDateTime scheduledAt;
        public BookingStatus status;
        public String address;
        public String notes;
    }
}