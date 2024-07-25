package dev.nncode.gooutbackend.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import dev.nncode.gooutbackend.user.model.Role;
import dev.nncode.gooutbackend.user.repository.RoleRepository;

@Service
public class RoleService {

    private final Logger logger = LoggerFactory.getLogger(RoleService.class);

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Iterable<Role> getAllRoles() {
        var availableRoles = roleRepository.findAll();
        logger.info("Get availableRoles: {}", availableRoles);
        return availableRoles;
    }

}
