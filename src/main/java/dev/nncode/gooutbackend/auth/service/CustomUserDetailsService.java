package dev.nncode.gooutbackend.auth.service;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import dev.nncode.gooutbackend.auth.dto.AuthenticatedUser;
import dev.nncode.gooutbackend.auth.repository.UserLoginRepository;
import dev.nncode.gooutbackend.common.enumeration.RoleEnum;
import dev.nncode.gooutbackend.common.exception.EntityNotFoundException;
import dev.nncode.gooutbackend.tourcompany.repository.TourCompanyLoginRepository;
import dev.nncode.gooutbackend.user.repository.UserRoleRepository;

@Primary
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserLoginRepository userLoginRepository;
    private final UserRoleRepository userRoleRepository;
    private final TourCompanyLoginRepository tourCompanyLoginRepository;

    public CustomUserDetailsService(UserLoginRepository userLoginRepository, UserRoleRepository userRoleRepository,
            TourCompanyLoginRepository tourCompanyLoginRepository) {
        this.userLoginRepository = userLoginRepository;
        this.userRoleRepository = userRoleRepository;
        this.tourCompanyLoginRepository = tourCompanyLoginRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if (isEmail(username)) {
            return userFlow(username);
        }
        return TourCompanyFlow(username);
    }

    private boolean isEmail(String username) {
        return EmailValidator.getInstance().isValid(username);
    }

    private AuthenticatedUser userFlow(String username) {
        var userLogin = userLoginRepository.findOneByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Credential for %s not found", username)));
        var userId = userLogin.userId().getId();
        var userRole = userRoleRepository.findOneByUserId(AggregateReference.to(userId))
                .orElseThrow(
                        () -> new EntityNotFoundException(String.format("Role for username: %s not found", username)));
        var role = RoleEnum.CONSUMER;
        if (userRole.roleId().getId() == RoleEnum.ADMIN.getId()) {
            role = RoleEnum.ADMIN;
        }
        return new AuthenticatedUser(userId, userLogin.email(), userLogin.password(), role);
    }

    private UserDetails TourCompanyFlow(String username) {
        var tourCompanyLogin = tourCompanyLoginRepository.findOneByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Credential for %s not found", username)));
        return new AuthenticatedUser(tourCompanyLogin.id(), tourCompanyLogin.username(), tourCompanyLogin.password(),
                RoleEnum.COMPANY);
    }
}
