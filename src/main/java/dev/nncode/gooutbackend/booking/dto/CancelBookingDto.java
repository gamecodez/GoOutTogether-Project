package dev.nncode.gooutbackend.booking.dto;

import jakarta.validation.constraints.NotNull;

public record CancelBookingDto(
                String idempotentKey,
                @NotNull Integer bookingId,
                @NotNull Integer userId,
                @NotNull Integer tourId) {
}
