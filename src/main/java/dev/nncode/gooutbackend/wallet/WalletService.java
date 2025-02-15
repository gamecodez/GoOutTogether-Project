package dev.nncode.gooutbackend.wallet;

import java.math.BigDecimal;

import org.springframework.data.util.Pair;

import dev.nncode.gooutbackend.booking.model.Booking;
import dev.nncode.gooutbackend.common.enumeration.TransactionType;
import dev.nncode.gooutbackend.wallet.dto.TopupDto;
import dev.nncode.gooutbackend.wallet.dto.UserWalletInfoDto;
import dev.nncode.gooutbackend.wallet.model.TourCompanyWallet;
import dev.nncode.gooutbackend.wallet.model.UserWallet;

public interface WalletService {

    UserWallet createConsumerWallet(int userId);

    void deleteConsumerWalletByUserId(int userId);

    UserWalletInfoDto getOwnWallet(int userId);

    UserWalletInfoDto topup(TopupDto body);

    Pair<UserWallet, TourCompanyWallet> getUserWalletAndTourCompanyWallet(Booking bookingData);

    Pair<UserWallet, TourCompanyWallet> transfer(UserWallet userWallet,
            TourCompanyWallet tourCompanyWallet,
            BigDecimal amount,
            TransactionType type);
}
