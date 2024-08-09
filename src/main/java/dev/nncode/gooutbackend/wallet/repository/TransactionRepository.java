package dev.nncode.gooutbackend.wallet.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import dev.nncode.gooutbackend.wallet.model.Transaction;

public interface TransactionRepository extends CrudRepository<Transaction, Integer> {

    Optional<Transaction> findOneByIdempotentKey(String idempotentKey);

}
