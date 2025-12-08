package com.servicelink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class ListingDtos {
    public static class CreateRequest {
        @NotBlank public String title;
        public String description;
        @NotNull @Positive public BigDecimal price;
        public Long categoryId; // optional
    }
    public static class UpdateRequest {
        @NotBlank public String title;
        public String description;
        @NotNull @Positive public BigDecimal price;
        public Long categoryId; // optional
    }
    public static class Response {
        public Long id;
        public String title;
        public String description;
        public BigDecimal price;
        public Long ownerId;
        public String ownerName;
        public Long categoryId;
        public String categoryName;
    }
}
