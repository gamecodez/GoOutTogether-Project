package dev.nncode.gooutbackend.user.repository;

import org.springframework.data.repository.CrudRepository;

import dev.nncode.gooutbackend.user.model.UserRole;

public interface UserRoleRepository extends CrudRepository<UserRole, Integer> {

}
