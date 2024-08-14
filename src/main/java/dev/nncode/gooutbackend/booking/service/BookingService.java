package dev.nncode.gooutbackend.booking.service;

import org.springframework.security.core.Authentication;

import dev.nncode.gooutbackend.booking.dto.BookingInfoDto;
import dev.nncode.gooutbackend.booking.dto.CancelBookingDto;
import dev.nncode.gooutbackend.booking.dto.RequestBookingDto;

public interface BookingService {

    BookingInfoDto bookTour(Authentication authentication, RequestBookingDto body);

    BookingInfoDto cancelTour(Authentication authentication, CancelBookingDto body);

}
