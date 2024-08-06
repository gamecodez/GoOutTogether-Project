package dev.nncode.gooutbackend.tourcompany.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import dev.nncode.gooutbackend.tourcompany.model.TourCompanyLogin;

public interface TourCompanyLoginRepository extends CrudRepository<TourCompanyLogin, Integer> {

    Optional<TourCompanyLogin> findOneByUsername(String username);

}
