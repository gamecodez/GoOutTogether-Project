package dev.nncode.gooutbackend.booking.dto;

public record BookingInfoDto(
                Integer bookingId,
                Integer userId,
                Integer tourId,
                String state,
                Integer qrReference) {
}
