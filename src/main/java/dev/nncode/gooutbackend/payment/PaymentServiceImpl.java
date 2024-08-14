package dev.nncode.gooutbackend.payment;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.nncode.gooutbackend.booking.dto.BookingInfoDto;
import dev.nncode.gooutbackend.booking.model.Booking;
import dev.nncode.gooutbackend.booking.repository.BookingRepository;
import dev.nncode.gooutbackend.common.enumeration.BookingStatusEnum;
import dev.nncode.gooutbackend.common.enumeration.QrCodeStatus;
import dev.nncode.gooutbackend.common.enumeration.TransactionType;
import dev.nncode.gooutbackend.common.exception.EntityNotFoundException;
import dev.nncode.gooutbackend.qrcode.QrCodeService;
import dev.nncode.gooutbackend.tour.service.TourCountService;
import dev.nncode.gooutbackend.wallet.WalletService;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final QrCodeService qrCodeService;
    private final BookingRepository bookingRepository;
    private final WalletService walletService;
    private final TransactionRepository transactionRepository;
    private final TourCountService tourCountService;
    private final int tourPrice;

    public PaymentServiceImpl(QrCodeService qrCodeService, BookingRepository bookingRepository,
            WalletService walletService, TransactionRepository transactionRepository,
            TourCountService tourCountService,
            @Value(value = "${booking.tour-price}") int tourPrice) {
        this.qrCodeService = qrCodeService;
        this.bookingRepository = bookingRepository;
        this.walletService = walletService;
        this.transactionRepository = transactionRepository;
        this.tourCountService = tourCountService;
        this.tourPrice = tourPrice;
    }

    @Override
    public BufferedImage generatePaymentQr(int id) throws Exception {
        return qrCodeService.generateQrById(id);
    }

    @Override
    @Transactional
    public BookingInfoDto paymentOnBooking(String idempotentKey, int bookingId) {
        // idempotentKey - use in transaction
        var bookingData = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("BookingId %s not found", bookingId)));
        var wallets = walletService.getUserWalletAndTourCompanyWallet(bookingData);
        var userWallet = wallets.getFirst();
        var tourCompanyWallet = wallets.getSecond();
        // UserWallet -
        // TourCompanyWallet +
        walletService.transfer(
                userWallet,
                tourCompanyWallet,
                BigDecimal.valueOf(tourPrice),
                TransactionType.BOOKING);

        var newTransaction = TransactionUtil.generateBookingTransaction(
                idempotentKey,
                bookingId,
                userWallet.userId().getId(),
                tourCompanyWallet.tourCompanyId().getId(),
                Instant.now(),
                BigDecimal.valueOf(tourPrice));
        transactionRepository.save(newTransaction);
        var qrCodeReference = qrCodeService.updatedQrStatus(bookingId, QrCodeStatus.EXPIRED);
        // Update booking to completed
        var prepareUpdateBooking = new Booking(
                bookingData.id(),
                bookingData.userId(),
                bookingData.tourId(),
                BookingStatusEnum.COMPLETED.name(),
                bookingData.bookingDate(),
                Instant.now(),
                idempotentKey);
        bookingRepository.save(prepareUpdateBooking);
        // Update tour count
        tourCountService.incrementTourCount(bookingData.tourId().getId());
        return new BookingInfoDto(
                bookingData.id(),
                bookingData.userId().getId(),
                bookingData.tourId().getId(),
                BookingStatusEnum.COMPLETED.name(),
                qrCodeReference.id());
    }

    @Override
    @Transactional
    public void refundOnBooking(String idempotent, int bookingId) {
        var bookingData = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("BookingId %s not found", bookingId)));
        var wallets = walletService.getUserWalletAndTourCompanyWallet(bookingData);
        var userWallet = wallets.getFirst();
        var tourCompanyWallet = wallets.getSecond();
        // UserWallet +
        // TourCompanyWallet -
        walletService.transfer(
                userWallet,
                tourCompanyWallet,
                BigDecimal.valueOf(tourPrice),
                TransactionType.REFUND);
        var newTransaction = TransactionUtil.generateRefundTransaction(
                idempotent,
                bookingId,
                userWallet.userId().getId(),
                tourCompanyWallet.tourCompanyId().getId(),
                Instant.now(),
                BigDecimal.valueOf(tourPrice));
        transactionRepository.save(newTransaction);
    }
}
