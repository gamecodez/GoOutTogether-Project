package dev.nncode.gooutbackend.wallet;

import java.math.BigDecimal;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.nncode.gooutbackend.booking.model.Booking;
import dev.nncode.gooutbackend.common.enumeration.TransactionType;
import dev.nncode.gooutbackend.common.exception.EntityNotFoundException;
import dev.nncode.gooutbackend.payment.Transaction;
import dev.nncode.gooutbackend.payment.TransactionRepository;
import dev.nncode.gooutbackend.payment.TransactionUtil;
import dev.nncode.gooutbackend.tour.repository.TourRepository;
import dev.nncode.gooutbackend.user.model.User;
import dev.nncode.gooutbackend.wallet.dto.TopupDto;
import dev.nncode.gooutbackend.wallet.dto.UserWalletInfoDto;
import dev.nncode.gooutbackend.wallet.model.TourCompanyWallet;
import dev.nncode.gooutbackend.wallet.model.UserWallet;
import dev.nncode.gooutbackend.wallet.repository.TourCompanyWalletRepository;
import dev.nncode.gooutbackend.wallet.repository.UserWalletRepository;

@Service
public class WalletServiceImpl implements WalletService {

    private final Logger logger = LoggerFactory.getLogger(WalletServiceImpl.class);

    private final UserWalletRepository userWalletRepository;
    private final TransactionRepository transactionRepository;
    private final TourCompanyWalletRepository tourCompanyWalletRepository;
    private final TourRepository tourRepository;

    public WalletServiceImpl(UserWalletRepository userWalletRepository, TransactionRepository transactionRepository,
            TourCompanyWalletRepository tourCompanyWalletRepository, TourRepository tourRepository) {
        this.userWalletRepository = userWalletRepository;
        this.transactionRepository = transactionRepository;
        this.tourCompanyWalletRepository = tourCompanyWalletRepository;
        this.tourRepository = tourRepository;
    }

    @Override
    public UserWallet createConsumerWallet(int userId) {
        AggregateReference<User, Integer> userReference = AggregateReference.to(userId);
        Instant currentTimestamp = Instant.now();
        BigDecimal initBalance = new BigDecimal("0.00");
        var wallet = new UserWallet(null, userReference, currentTimestamp, initBalance);
        var newWallet = userWalletRepository.save(wallet);
        logger.info("Created wallet for user: {}", userId);
        return newWallet;
    }

    @Override
    public void deleteConsumerWalletByUserId(int userId) {
        AggregateReference<User, Integer> userReference = AggregateReference.to(userId);
        var wallet = userWalletRepository
                .findOneByUserId(userReference)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Wallet for user Id: %d not found", userId)));
        userWalletRepository.delete(wallet);
    }

    @Override
    @Transactional
    public UserWalletInfoDto topup(TopupDto body) {
        var now = Instant.now();
        var idempotentKey = body.idempotentKey();
        var userId = body.userId();
        var userWallet = getWalletByUserId(userId);
        var optionalHistoricalTranaction = transactionRepository.findOneByIdempotentKey(idempotentKey);
        if (optionalHistoricalTranaction.isPresent()) {
            return new UserWalletInfoDto(userWallet.userId().getId(), userWallet.balance());
        }
        var newTransaction = TransactionUtil.generateTopupTransaction(idempotentKey, userId, now, body.amount());
        transactionRepository.save(newTransaction);
        var updatedBalance = userWallet.balance().add(body.amount());
        var updatedTopupBalance = new UserWallet(userWallet.id(), userWallet.userId(), now, updatedBalance);
        var updatedWallet = userWalletRepository.save(updatedTopupBalance);
        return new UserWalletInfoDto(updatedWallet.userId().getId(), updatedWallet.balance());
    }

    @Override
    public UserWalletInfoDto getOwnWallet(int userId) {
        var userWallet = getWalletByUserId(userId);
        return new UserWalletInfoDto(userId, userWallet.balance());
    }

    private UserWallet getWalletByUserId(int userId) {
        return userWalletRepository
                .findOneByUserId(AggregateReference.to(userId))
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Wallet for userId: %d not found", userId)));
    }

    @Override
    public Pair<UserWallet, TourCompanyWallet> getUserWalletAndTourCompanyWallet(Booking bookingData) {

        var userId = bookingData.userId();
        var tourId = bookingData.tourId();
        var userWallet = userWalletRepository.findOneByUserId(userId)
                .orElseThrow(EntityNotFoundException::new);
        var tourInfo = tourRepository.findById(tourId.getId())
                .orElseThrow(EntityNotFoundException::new);
        var tourCompanyWallet = tourCompanyWalletRepository.findOneByTourCompanyId(tourInfo.tourCompanyId())
                .orElseThrow(EntityNotFoundException::new);
        return Pair.of(userWallet, tourCompanyWallet);
    }

    @Override
    @Transactional
    public Pair<UserWallet, TourCompanyWallet> transfer(UserWallet userWallet, TourCompanyWallet tourCompanyWallet,
            BigDecimal amount, TransactionType type) {
        return switch (type) {
            case TransactionType.BOOKING -> {
                var prepareUserWallet = new UserWallet(
                        userWallet.id(),
                        userWallet.userId(),
                        Instant.now(),
                        userWallet.balance().subtract(amount));
                var prepareTourCompanyWallet = new TourCompanyWallet(
                        tourCompanyWallet.id(),
                        tourCompanyWallet.tourCompanyId(),
                        Instant.now(),
                        tourCompanyWallet.balance().add(amount));
                var updateUserWallet = userWalletRepository.save(prepareUserWallet);
                var updateTourCompanyWallet = tourCompanyWalletRepository.save(prepareTourCompanyWallet);
                yield Pair.of(updateUserWallet, updateTourCompanyWallet);
            }
            case TransactionType.REFUND -> {
                var prepareUserWallet = new UserWallet(
                        userWallet.id(),
                        userWallet.userId(),
                        Instant.now(),
                        userWallet.balance().add(amount));
                var prepareTourCompanyWallet = new TourCompanyWallet(
                        tourCompanyWallet.id(),
                        tourCompanyWallet.tourCompanyId(),
                        Instant.now(),
                        tourCompanyWallet.balance().subtract(amount));
                var updateUserWallet = userWalletRepository.save(prepareUserWallet);
                var updateTourCompanyWallet = tourCompanyWalletRepository.save(prepareTourCompanyWallet);
                yield Pair.of(updateUserWallet, updateTourCompanyWallet);
            }
            default -> {
                throw new IllegalStateException("Invalid Transaction Type");
            }
        };
    }
}
