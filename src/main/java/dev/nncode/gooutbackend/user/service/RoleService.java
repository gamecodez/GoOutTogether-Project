package dev.nncode.gooutbackend.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

import dev.nncode.gooutbackend.common.enumeration.RoleEnum;
import dev.nncode.gooutbackend.user.model.Role;
import dev.nncode.gooutbackend.user.model.User;
import dev.nncode.gooutbackend.user.model.UserRole;
import dev.nncode.gooutbackend.user.repository.RoleRepository;
import dev.nncode.gooutbackend.user.repository.UserRoleRepository;

@Service
public class RoleService {

    private final Logger logger = LoggerFactory.getLogger(RoleService.class);

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public RoleService(RoleRepository roleRepository, dev.nncode.gooutbackend.user.repository.UserRoleRepository userRoleRepository) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    public Iterable<Role> getAllRoles() {
        var availableRoles = roleRepository.findAll();
        logger.info("Get availableRoles: {}", availableRoles);
        return availableRoles;
    }

    public UserRole bindingNewUser(int id, RoleEnum role) {
        AggregateReference<User, Integer> userId = AggregateReference.to(id);
        AggregateReference<Role, Integer> roleId = AggregateReference.to(role.getId());
        var prepareRole = new UserRole(null, userId, roleId);
        return userRoleRepository.save(prepareRole);
    }
}
