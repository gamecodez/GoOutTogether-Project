package dev.nncode.gooutbackend.wallet;

import dev.nncode.gooutbackend.wallet.dto.TopupDto;
import dev.nncode.gooutbackend.wallet.dto.UserWalletInfoDto;
import dev.nncode.gooutbackend.wallet.model.UserWallet;

public interface WalletService {

    UserWallet createConsumerWallet(int userId);

    void deleteConsumerWalletByUserId(int userId);

    UserWalletInfoDto getOwnWallet(int userId);

    UserWalletInfoDto topup(TopupDto body);

}
