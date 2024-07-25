package dev.nncode.gooutbackend.tour.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TourDto(
        @NotNull Integer tourCompanyId,
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String location,
        @NotNull Integer numberOfPeople,
        @NotNull Instant activityDate,
        String status) {

}
