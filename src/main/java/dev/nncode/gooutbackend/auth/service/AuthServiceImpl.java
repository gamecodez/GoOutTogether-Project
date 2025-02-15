package dev.nncode.gooutbackend.auth.service;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.nncode.gooutbackend.auth.dto.AuthenticatedUser;
import dev.nncode.gooutbackend.auth.dto.LoginRequestDto;
import dev.nncode.gooutbackend.auth.dto.LoginResponseDto;
import dev.nncode.gooutbackend.auth.dto.LogoutDto;
import dev.nncode.gooutbackend.auth.dto.RefreshTokenDto;
import dev.nncode.gooutbackend.auth.model.RefreshToken;
import dev.nncode.gooutbackend.auth.model.UserLogin;
import dev.nncode.gooutbackend.auth.repository.RefreshTokenRepository;
import dev.nncode.gooutbackend.auth.repository.UserLoginRepository;
import static dev.nncode.gooutbackend.common.Constants.TOKEN_TYPE;
import dev.nncode.gooutbackend.common.enumeration.RoleEnum;
import dev.nncode.gooutbackend.common.exception.EntityNotFoundException;
import dev.nncode.gooutbackend.common.exception.RefreshTokenExpiredException;
import dev.nncode.gooutbackend.tourcompany.model.TourCompany;
import dev.nncode.gooutbackend.tourcompany.repository.TourCompanyLoginRepository;
import dev.nncode.gooutbackend.user.model.User;
import dev.nncode.gooutbackend.user.repository.UserRepository;

@Service
public class AuthServiceImpl implements AuthService {

        private final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

        private final UserLoginRepository userLoginRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final TokenService tokenService;
        private final RefreshTokenRepository refreshTokenRepository;
        private final UserRepository userRepository;
        private final TourCompanyLoginRepository tourCompanyLoginRepository;

        public AuthServiceImpl(AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder,
                        TokenService tokenService, UserLoginRepository userLoginRepository,
                        RefreshTokenRepository refreshTokenRepository, UserRepository userRepository,
                        TourCompanyLoginRepository tourCompanyLoginRepository) {
                this.authenticationManager = authenticationManager;
                this.passwordEncoder = passwordEncoder;
                this.tokenService = tokenService;
                this.userLoginRepository = userLoginRepository;
                this.refreshTokenRepository = refreshTokenRepository;
                this.userRepository = userRepository;
                this.tourCompanyLoginRepository = tourCompanyLoginRepository;

        }

        @Override
        public Optional<UserLogin> findCredentialByUsername(String email) {
                return userLoginRepository.findOneByEmail(email);
        }

        @Override
        public UserLogin createConsumerCredential(int userId, String email, String password) {
                AggregateReference<User, Integer> userReference = AggregateReference.to(userId);
                var encryptedPassword = passwordEncoder.encode(password);
                var userCredential = new UserLogin(null, userReference, email, encryptedPassword);
                var createdCredential = userLoginRepository.save(userCredential);
                logger.info("Created credential for user: {}", userId);
                return createdCredential;
        }

        @Override
        public Optional<UserLogin> findCredentialByUserId(int userId) {
                AggregateReference<User, Integer> userReference = AggregateReference.to(userId);
                return userLoginRepository.findOneByUserId(userReference);
        }

        @Override
        public void deleteCredentialByUserId(int userId) {
                var credential = findCredentialByUserId(userId)
                                .orElseThrow(
                                                () -> new EntityNotFoundException(
                                                                String.format("Credential for user Id: %d not found",
                                                                                userId)));
                userLoginRepository.delete(credential);
        }

        @Override
        @Transactional
        public LoginResponseDto login(LoginRequestDto body) {
                var authInfo = new UsernamePasswordAuthenticationToken(body.username(), body.password());
                var authentication = authenticationManager.authenticate(authInfo);
                var authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();

                var now = Instant.now();
                var accessToken = tokenService.issueAccessToken(authentication, now);
                var refreshToken = tokenService.issueRefreshToken();

                logout(authentication);

                refreshTokenRepository.updateRefreshTokenByResource(
                                authenticatedUser.role().name(),
                                authenticatedUser.userId(),
                                true);
                // Save refresh token
                var prepareRefreshTokenModel = new RefreshToken(
                                null,
                                refreshToken,
                                now,
                                authenticatedUser.role().name(),
                                authenticatedUser.userId(),
                                false);

                refreshTokenRepository.save(prepareRefreshTokenModel);

                return new LoginResponseDto(
                                authenticatedUser.userId(),
                                TOKEN_TYPE,
                                accessToken,
                                refreshToken);
        }

        @Override
        public void logout(Authentication authentication) {

                var authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
                refreshTokenRepository.updateRefreshTokenByResource(
                                authenticatedUser.role().name(),
                                authenticatedUser.userId(),
                                true);
        }

        @Override
        public void logout(LogoutDto logoutDto) {
                refreshTokenRepository.updateRefreshTokenByResource(
                                logoutDto.roles(),
                                Integer.parseInt((logoutDto.sub())),
                                true);
        }

        @Override
        @Transactional
        public LoginResponseDto issueNewAccessToken(RefreshTokenDto body) {
                // Check refresh token is exists?
                var refreshTokenEntity = refreshTokenRepository.findOneByToken(body.refreshToken())
                                .orElseThrow(() -> new EntityNotFoundException("This refresh token not found"));
                var resourceId = refreshTokenEntity.resourceId();
                // Expired? - DB -> IssuedDate + configured expire time
                if (tokenService.isRefreshTokenExpired(refreshTokenEntity)) {
                        logout(new LogoutDto(String.valueOf(resourceId), refreshTokenEntity.usage()));
                        // Need re-login
                        throw new RefreshTokenExpiredException("This refresh token is expired");
                }
                // Token almost expired => refresh token rotation
                String newAccessToken = switch (RoleEnum.valueOf(body.usage())) {
                        case RoleEnum.COMPANY -> {
                                AggregateReference<TourCompany, Integer> tourCompanyReference = AggregateReference
                                                .to(resourceId);
                                var credential = tourCompanyLoginRepository.findOneByTourCompanyId(tourCompanyReference)
                                                .orElseThrow(() -> new EntityNotFoundException(
                                                                String.format("Credential for company Id: %d not found",
                                                                                resourceId)));
                                yield tokenService.issueAccessToken(credential, Instant.now());
                        }
                        default -> {
                                var user = userRepository.findById(resourceId)
                                                .orElseThrow(() -> new EntityNotFoundException(
                                                                String.format("User Id: %d not found", resourceId)));
                                var credential = findCredentialByUserId(user.id())
                                                .orElseThrow(
                                                                () -> new EntityNotFoundException(
                                                                                String.format("Credential for user Id: %d not found",
                                                                                                user.id())));
                                yield tokenService.issueAccessToken(credential, Instant.now());
                        }
                };
                var refreshToken = tokenService.rotateRefreshTokenIfNeed(refreshTokenEntity);
                // Check if refresh token change -> change old refresh token to expired
                if (!refreshToken.equals(refreshTokenEntity.token())) {
                        var updatedRefreshTokenEntity = new RefreshToken(
                                        refreshTokenEntity.id(),
                                        refreshTokenEntity.token(),
                                        refreshTokenEntity.issuedDate(),
                                        refreshTokenEntity.usage(),
                                        refreshTokenEntity.resourceId(),
                                        true);
                        refreshTokenRepository.save(updatedRefreshTokenEntity);
                        var prepareRefreshTokenModel = new RefreshToken(
                                        null,
                                        refreshToken,
                                        Instant.now(),
                                        refreshTokenEntity.usage(),
                                        refreshTokenEntity.resourceId(),
                                        false);
                        refreshTokenRepository.save(prepareRefreshTokenModel);
                }
                return new LoginResponseDto(
                                refreshTokenEntity.resourceId(),
                                TOKEN_TYPE,
                                newAccessToken,
                                refreshToken);
        }
}
