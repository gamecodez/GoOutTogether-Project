package dev.nncode.gooutbackend.booking.repository;

import java.util.Optional;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;

import dev.nncode.gooutbackend.booking.model.Booking;
import dev.nncode.gooutbackend.tour.model.Tour;
import dev.nncode.gooutbackend.user.model.User;

public interface BookingRepository extends CrudRepository<Booking, Integer> {

    Optional<Booking> findOneByIdempotentKey(String idempotentKey);

    Optional<Booking> findOneByUserIdAndTourId(AggregateReference<User, Integer> userId,
            AggregateReference<Tour, Integer> tourId);

}
