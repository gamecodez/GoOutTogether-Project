package dev.nncode.gooutbackend.user.repository;

import org.springframework.data.repository.CrudRepository;

import dev.nncode.gooutbackend.user.model.User;

public interface UserRepository extends CrudRepository<User, Integer> {

}
