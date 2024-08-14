package dev.nncode.gooutbackend.booking.dto;

import jakarta.validation.constraints.NotNull;

public record RequestBookingDto(
                String idempotentKey,
                @NotNull Integer userId,
                @NotNull Integer tourId) {

}
