package dev.nncode.gooutbackend.user.repository;

import org.springframework.data.repository.CrudRepository;

import dev.nncode.gooutbackend.user.model.Role;

public interface RoleRepository extends  CrudRepository<Role, Integer> {

}
