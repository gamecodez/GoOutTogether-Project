package dev.nncode.gooutbackend.booking.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

import dev.nncode.gooutbackend.tour.model.Tour;
import dev.nncode.gooutbackend.user.model.User;

@Table("booking")
public record Booking(
                @Id Integer id,
                AggregateReference<User, Integer> userId,
                AggregateReference<Tour, Integer> tourId,
                String state,
                Instant bookingDate,
                Instant lastUpdated,
                String idempotentKey) {

}
