package dev.nncode.gooutbackend.wallet.repository;

import java.util.Optional;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;

import dev.nncode.gooutbackend.tourcompany.model.TourCompany;
import dev.nncode.gooutbackend.wallet.model.TourCompanyWallet;

public interface TourCompanyWalletRepository extends CrudRepository<TourCompanyWallet, Integer> {

    Optional<TourCompanyWallet> findOneByTourCompanyId(AggregateReference<TourCompany, Integer> tourCompanyId);

}
