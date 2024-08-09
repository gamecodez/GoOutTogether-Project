package dev.nncode.gooutbackend.wallet.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;

public record TopupDto(
        @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
        Integer userId,
        String idempotentKey) {

}
