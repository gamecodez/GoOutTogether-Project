package dev.nncode.gooutbackend.wallet;

import dev.nncode.gooutbackend.wallet.model.UserWallet;

public interface WalletService {

    UserWallet createConsumerWallet(int userId);

    void deleteConsumerWalletByUserId(int userId);

}
