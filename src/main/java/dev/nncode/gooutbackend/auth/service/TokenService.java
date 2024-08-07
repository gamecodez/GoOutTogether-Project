package dev.nncode.gooutbackend.auth.service;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import dev.nncode.gooutbackend.auth.dto.AuthenticatedUser;
import dev.nncode.gooutbackend.auth.model.RefreshToken;
import dev.nncode.gooutbackend.auth.model.UserLogin;
import dev.nncode.gooutbackend.tourcompany.model.TourCompanyLogin;

@Service
public class TokenService {

    private static final String ISSUER = "gout-backend";
    private static final String ROLES_CLAIM = "roles";
    private static final int TIME_FOR_ROTATE_SECONDS = 120;

    private final JwtEncoder jwtEncoder;
    private final long accessTokenExpiredInSeconds;
    private final long refreshTokenExpiredInSeconds;
    private final CustomUserDetailsService customUserDetailsService;

    public TokenService(
            JwtEncoder jwtEncoder,
            @Value(value = "${token.access-token-expired-in-seconds}") long accessTokenExpiredInSeconds,
            @Value(value = "${token.refresh-token-expired-in-seconds}") long refreshTokenExpiredInSeconds,
            CustomUserDetailsService customUserDetailsService) {
        this.jwtEncoder = jwtEncoder;
        this.accessTokenExpiredInSeconds = accessTokenExpiredInSeconds;
        this.refreshTokenExpiredInSeconds = refreshTokenExpiredInSeconds;
        this.customUserDetailsService = customUserDetailsService;
    }

    public String issueAccessToken(Authentication auth, Instant issueDate) {
        return generateToken(auth, issueDate, accessTokenExpiredInSeconds);
    }
    public String issueAccessToken(UserLogin userLogin, Instant issuedDate) {
        AuthenticatedUser userDetails = (AuthenticatedUser) customUserDetailsService
                .loadUserByUsername(userLogin.email());
        return generateToken(userDetails, issuedDate, accessTokenExpiredInSeconds);
    }

    public String issueAccessToken(TourCompanyLogin tourCompanyLogin, Instant issuedDate) {
        AuthenticatedUser userDetails = (AuthenticatedUser) customUserDetailsService
                .loadUserByUsername(tourCompanyLogin.username());
        return generateToken(userDetails, issuedDate, accessTokenExpiredInSeconds);
    }


    public String issueRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public String generateToken(AuthenticatedUser auth, Instant issuedDate, long expiredInSeconds) {
        return generateToken(auth.userId(), auth.getAuthorities(), issuedDate, expiredInSeconds);
    }

    public String generateToken(Authentication auth, Instant issueDate, long expiredInSeconds) {
        var authenticatedUser = (AuthenticatedUser) auth.getPrincipal();
        return generateToken(authenticatedUser.userId(), authenticatedUser.getAuthorities(), issueDate, expiredInSeconds);

    }
    private String generateToken(Integer userId,
            Collection<? extends GrantedAuthority> authorities,
            Instant issuedDate,
            long expiredInSeconds) {

        Instant expire = issuedDate.plusSeconds(expiredInSeconds);

        String scope = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(issuedDate)
                .subject(String.valueOf(userId))
                .claim(ROLES_CLAIM, scope)
                .expiresAt(expire)
                .build();
        return encodeClaimToJwt(claims);
    }

    public String encodeClaimToJwt(JwtClaimsSet claims) {
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public boolean isRefreshTokenExpired(RefreshToken refreshToken) {
        var issuedDate = refreshToken.issuedDate();
        var expiredDate = issuedDate.plusSeconds(refreshTokenExpiredInSeconds);
        var now = Instant.now();
        return now.isAfter(expiredDate);
    }

    public String rotateRefreshTokenIfNeed(RefreshToken refreshTokenEntity) {
        var issuedDate = refreshTokenEntity.issuedDate();
        var expiredDate = issuedDate.plusSeconds(refreshTokenExpiredInSeconds);
        var thresholdToRotateDate = expiredDate.minusSeconds(TIME_FOR_ROTATE_SECONDS);
        var now = Instant.now();
        if (now.isAfter(thresholdToRotateDate)) {
            return issueRefreshToken();
        }
        return refreshTokenEntity.token();
    }
}
