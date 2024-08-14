package dev.nncode.gooutbackend.booking.controller;

import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.nncode.gooutbackend.booking.dto.BookingInfoDto;
import dev.nncode.gooutbackend.booking.dto.CancelBookingDto;
import dev.nncode.gooutbackend.booking.dto.RequestBookingDto;
import dev.nncode.gooutbackend.booking.service.BookingService;

@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingInfoDto bookTour(
            @RequestHeader("idempotent-key") String idempotentKey,
            @RequestBody @Validated RequestBookingDto body,
            Authentication authentication) {
        RequestBookingDto updateBody = new RequestBookingDto(idempotentKey, body.userId(), body.tourId());
        return bookingService.bookTour(authentication, updateBody);
    }

    @PostMapping("/cancel")
    public BookingInfoDto cancelTour(
            @RequestHeader("idempotent-key") String idempotentKey,
            @RequestBody @Validated CancelBookingDto body,
            Authentication authentication) {
        CancelBookingDto updateBody = new CancelBookingDto(idempotentKey, body.bookingId(), body.userId(),
                body.tourId());
        return bookingService.cancelTour(authentication, updateBody);
    }
}
