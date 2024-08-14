package dev.nncode.gooutbackend.qrcode;

import java.awt.image.BufferedImage;

import org.springframework.stereotype.Service;

import static dev.nncode.gooutbackend.common.Constants.PAYMENT_PATH;
import dev.nncode.gooutbackend.common.enumeration.QrCodeStatus;
import dev.nncode.gooutbackend.common.exception.EntityNotFoundException;
import dev.nncode.gooutbackend.common.helper.QrCodeHelper;

@Service
public class QrCodeService {

    private final QrCodeReferenceRepository qrCodeReferenceRepository;

    public QrCodeService(QrCodeReferenceRepository qrCodeReferenceRepository) {
        this.qrCodeReferenceRepository = qrCodeReferenceRepository;
    }

    public BufferedImage generateQrById(int id) throws Exception {
        var optionalQrCodeRef = qrCodeReferenceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("QR Id: %d not found", id)));
        return QrCodeHelper.generateQRCodeImage(optionalQrCodeRef.content());
    }

    public QrCodeReference generateQrForBooking(int bookingId) {
        var optionalQrCodeRef = qrCodeReferenceRepository.findOneByBookingId(bookingId);
        if (optionalQrCodeRef.isPresent()) {
            return optionalQrCodeRef.get();
        }
        var paymentApiPath = String.format("%s/%d", PAYMENT_PATH, bookingId);
        var qrCodeEntity = new QrCodeReference(null, bookingId, paymentApiPath, QrCodeStatus.ACTIVATED.name());
        return qrCodeReferenceRepository.save(qrCodeEntity);
    }

    public QrCodeReference updatedQrStatus(int bookingId, QrCodeStatus status) {
        var optionalQrCodeRef = qrCodeReferenceRepository.findOneByBookingId(bookingId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("QR for bookingId: %d not found", bookingId)));
        var qrCodeEntity = new QrCodeReference(optionalQrCodeRef.id(),
                optionalQrCodeRef.bookingId(),
                optionalQrCodeRef.content(),
                status.name());
        return qrCodeReferenceRepository.save(qrCodeEntity);
    }

}
