package dev.nncode.gooutbackend.payment;

import java.awt.image.BufferedImage;

import dev.nncode.gooutbackend.booking.dto.BookingInfoDto;

public interface PaymentService {

    BufferedImage generatePaymentQr(int id) throws Exception;

    BookingInfoDto paymentOnBooking(String idempotentKey, int bookingId);

    void refundOnBooking(String idempotentKey, int bookingId);
}
