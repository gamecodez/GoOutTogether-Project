package dev.nncode.gooutbackend.wallet.dto;

import java.math.BigDecimal;

public record TourCompanyWalletInfoDto(
        Integer tourCompanyId,
        BigDecimal balance) {

}
