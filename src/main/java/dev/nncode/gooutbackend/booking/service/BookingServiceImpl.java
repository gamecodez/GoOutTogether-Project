package dev.nncode.gooutbackend.booking.service;

import java.time.Instant;
import java.util.Objects;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.nncode.gooutbackend.booking.dto.BookingInfoDto;
import dev.nncode.gooutbackend.booking.dto.CancelBookingDto;
import dev.nncode.gooutbackend.booking.dto.RequestBookingDto;
import dev.nncode.gooutbackend.booking.model.Booking;
import dev.nncode.gooutbackend.booking.repository.BookingRepository;
import dev.nncode.gooutbackend.common.enumeration.BookingStatusEnum;
import dev.nncode.gooutbackend.common.exception.BookingExistsException;
import dev.nncode.gooutbackend.common.exception.EntityNotFoundException;
import dev.nncode.gooutbackend.common.exception.UserIdMismatchException;
import dev.nncode.gooutbackend.payment.PaymentService;
import dev.nncode.gooutbackend.qrcode.QrCodeService;
import dev.nncode.gooutbackend.tour.service.TourCountService;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final TourCountService tourCountService;
    private final QrCodeService qrCodeService;
    private final PaymentService paymentService;

    public BookingServiceImpl(BookingRepository bookingRepository, TourCountService tourCountService,
            QrCodeService qrCodeService, PaymentService paymentService) {
        this.bookingRepository = bookingRepository;
        this.tourCountService = tourCountService;
        this.qrCodeService = qrCodeService;
        this.paymentService = paymentService;
    }

    @Override
    @Transactional
    public BookingInfoDto bookTour(Authentication authentication, RequestBookingDto body) {
        var jwt = (Jwt) authentication.getPrincipal();
        var userId = jwt.getClaimAsString("sub");
        if (!Objects.equals(Integer.valueOf(userId), body.userId())) {
            throw new UserIdMismatchException("User id mismatch between credential and payload");
        }
        var idempotentKey = body.idempotentKey();
        var existingBooking = bookingRepository.findOneByUserIdAndTourId(
            AggregateReference.to(body.userId()),
            AggregateReference.to(body.tourId()));
        if (existingBooking.isPresent()) {
            var data = existingBooking.get();
            if (data.state().equals(BookingStatusEnum.COMPLETED.name())) {
                throw new BookingExistsException(String.format(
                    "UserId: %d already booked tourId: %d",
                    data.userId().getId(),data.tourId().getId())); 
            }
            return new BookingInfoDto(
                data.id(),
                data.userId().getId(),
                data.tourId().getId(),
                data.state(),
                null);
        }
        var now = Instant.now();
        var newBooking = new Booking(
            null,
            AggregateReference.to(body.userId()),
            AggregateReference.to(body.tourId()),
            BookingStatusEnum.PENDING.name(),
            now,
            now,
            idempotentKey);
        var entity = bookingRepository.save(newBooking);
        // Generate QR Code
        var qrCodeReference = qrCodeService.generateQrForBooking(entity.id());
        return new BookingInfoDto(
            entity.id(),
            entity.userId().getId(),
            entity.tourId().getId(),
            entity.state(),
            qrCodeReference.id());
    }
    @Override
    @Transactional
    public BookingInfoDto cancelTour(Authentication authentication, CancelBookingDto body) {
       
        var jwt = (Jwt) authentication.getPrincipal();
        var userId = jwt.getClaimAsString("sub");
        if (!Objects.equals(Integer.valueOf(userId), body.userId())) {
            throw new UserIdMismatchException("User id mismatch between credential and payload");
        }
        var existingBooking = bookingRepository.findById(body.bookingId())
            .orElseThrow(() -> new EntityNotFoundException(String.format("BookingId %s not found", body.bookingId())));
        // Update tour count
        tourCountService.decrementTourCount(existingBooking.tourId().getId());
        // Refund payment
        paymentService.refundOnBooking(body.idempotentKey(), body.bookingId());
        // Delete booking for user
        bookingRepository.deleteById(body.bookingId());
        return new BookingInfoDto(
            existingBooking.id(),
            existingBooking.userId().getId(),
            existingBooking.tourId().getId(),
            BookingStatusEnum.CANCELLED.name(),
            null);            
    }
}
