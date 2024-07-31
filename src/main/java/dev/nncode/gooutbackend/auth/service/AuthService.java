package dev.nncode.gooutbackend.auth.service;

import java.util.Optional;

import dev.nncode.gooutbackend.auth.dto.LoginRequestDto;
import dev.nncode.gooutbackend.auth.dto.LoginResponseDto;
import dev.nncode.gooutbackend.auth.model.UserLogin;

public interface AuthService {

    Optional<UserLogin> findCredentialByUserId(int UserId);

    Optional<UserLogin> findCredentialByUsername(String email);

    UserLogin createConsumerCredential(int userId, String email, String password);

    void deleteCredentialByUserId(int userId);

    LoginResponseDto login(LoginRequestDto body);
}
