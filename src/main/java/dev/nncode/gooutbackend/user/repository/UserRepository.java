package dev.nncode.gooutbackend.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;

import dev.nncode.gooutbackend.user.model.User;

public interface UserRepository extends ListCrudRepository<User, Integer> {

    Page<User> findByFirstNameContaining(String firstName, Pageable pageable);

}
