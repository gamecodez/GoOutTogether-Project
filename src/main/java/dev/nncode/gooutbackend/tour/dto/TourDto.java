package dev.nncode.gooutbackend.tour.dto;

import java.time.Instant;

import org.springframework.data.jdbc.core.mapping.AggregateReference;

import dev.nncode.gooutbackend.tourcompany.model.TourCompany;
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
