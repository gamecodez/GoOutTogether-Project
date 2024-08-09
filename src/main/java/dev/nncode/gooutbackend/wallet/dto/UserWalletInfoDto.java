package dev.nncode.gooutbackend.wallet.dto;

import java.math.BigDecimal;

public record UserWalletInfoDto(
        Integer userId,
        BigDecimal balance) {

}
