package dev.nncode.gooutbackend.wallet.model;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

import dev.nncode.gooutbackend.user.model.User;

@Table("user_wallet")
public record UserWallet(
        @Id Integer id,
        AggregateReference<User, Integer> userId,
        Instant lastUpdated,
        BigDecimal balance) {

}
