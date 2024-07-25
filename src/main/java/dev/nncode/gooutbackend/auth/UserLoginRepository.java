package dev.nncode.gooutbackend.auth;

import java.util.Optional;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;

import dev.nncode.gooutbackend.user.model.User;

public interface UserLoginRepository extends CrudRepository<UserLogin, Integer> {

    Optional<UserLogin> findOneByEmail(String email);

    Optional<UserLogin> findOneByUserId(AggregateReference<User, Integer> userId);
}
