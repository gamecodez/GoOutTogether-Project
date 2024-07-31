package dev.nncode.gooutbackend.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.nncode.gooutbackend.auth.service.AuthService;
import dev.nncode.gooutbackend.common.enumeration.RoleEnum;
import dev.nncode.gooutbackend.common.exception.CredentialExistsException;
import dev.nncode.gooutbackend.common.exception.EntityNotFoundException;
import dev.nncode.gooutbackend.user.dto.UserCreationDto;
import dev.nncode.gooutbackend.user.dto.UserInfoDto;
import dev.nncode.gooutbackend.user.dto.UserUpdateDto;
import dev.nncode.gooutbackend.user.model.User;
import dev.nncode.gooutbackend.user.repository.UserRepository;
import dev.nncode.gooutbackend.wallet.WalletService;

@Service
public class UserServiceImpl implements UserService {

    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final WalletService walletService;
    private final RoleService roleService;
    private final AuthService authService;

    public UserServiceImpl(AuthService authService, UserRepository userRepository, WalletService walletService, RoleService roleService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.roleService = roleService;
    }

    @Override
    public UserInfoDto getUserDtoById(int id) {
        var user = getUserById(id);
        return new UserInfoDto(user.id(), user.firstName(), user.lastName(), user.phoneNumber());
    }

    @Override
    public User getUserById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User Id: %d not found", id)));
    }

    // Create user + login credential + wallet
    @Override
    @Transactional
    public UserInfoDto createUser(UserCreationDto body) {
        // 1. Find existing credential
        var existsCred = authService.findCredentialByUsername(body.email());
        if (existsCred.isPresent()) {
            throw new CredentialExistsException(String.format("User: %s exists!", body.email()));
        }
        // 2. Create user
        var prepareUser = new User(null, body.firstName(), body.lastName(), body.phoneNumber());
        var newUser = userRepository.save(prepareUser);
        // 3. Binding role
        var userRole = roleService.bindingNewUser(newUser.id(), RoleEnum.CONSUMER);
        // 4. Create credential
        var userCredential = authService.createConsumerCredential(newUser.id(), body.email(), body.password());
        // 5. Create wallet for user
        walletService.createConsumerWallet(newUser.id());
        return new UserInfoDto(newUser.id(), newUser.firstName(), newUser.lastName(), newUser.phoneNumber());
    }

    // Update user
    @Override
    public UserInfoDto updateUser(int id, UserUpdateDto body) {
        var user = getUserById(id);
        var prepareUser = new User(user.id(), body.firstName(), body.lastName(), user.phoneNumber());
        var updatedUser = userRepository.save(prepareUser);
        return new UserInfoDto(updatedUser.id(), updatedUser.firstName(), updatedUser.lastName(),
                updatedUser.phoneNumber());
    }

    // Delete user + credential & wallet removal
    @Override
    @Transactional
    public boolean deleteUserById(int id) {
        // 1. Find existing user
        var user = getUserById(id);
        // 2. Find every resource under this userId and delete
        authService.deleteCredentialByUserId(user.id());
        logger.info("Delete credential for userId: {}", user.id());
        walletService.deleteConsumerWalletByUserId(user.id());
        logger.info("Delete wallet for userId: {}", user.id());
        userRepository.delete(user);
        logger.info("Delete userId: {}", user.id());
        return true;
    }
}