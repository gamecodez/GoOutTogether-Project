package dev.nncode.gooutbackend.wallet.repository;

import java.util.Optional;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;

import dev.nncode.gooutbackend.user.model.User;
import dev.nncode.gooutbackend.wallet.model.UserWallet;

public interface UserWalletRepository extends CrudRepository<UserWallet, Integer> {
    
    Optional<UserWallet> findOneByUserId(AggregateReference<User, Integer> userId); 

}
