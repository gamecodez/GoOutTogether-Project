package dev.nncode.gooutbackend.auth.service;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import dev.nncode.gooutbackend.auth.dto.AuthenticatedUser;

@Service
public class TokenService {

    private static final String ISSUER = "gout-backend";
    private static final String ROLES_CLAIM = "roles";

    private final JwtEncoder jwtEncoder;
    private final long accessTokenExpiredInSeconds;
    private final long refreshTokenExpiredInSeconds;

    public TokenService(
            JwtEncoder jwtEncoder,
            @Value("${token.access-token-expired-in-seconds}") long accessTokenExpiredInSeconds,
            @Value("${token.refresh-token-expired-in-seconds}") long refreshTokenExpiredInSeconds) {
        this.jwtEncoder = jwtEncoder;
        this.accessTokenExpiredInSeconds = accessTokenExpiredInSeconds;
        this.refreshTokenExpiredInSeconds = refreshTokenExpiredInSeconds;
    }

    public String issueAccessToken(Authentication auth, Instant issuedDate) {
        return generateToken(auth, issuedDate, accessTokenExpiredInSeconds);
    }

    public String issueRefreshToken(Authentication auth, Instant issuedDate) {
        return generateToken(auth, issuedDate, refreshTokenExpiredInSeconds);
    }

    public String generateToken(Authentication auth, Instant issuedDate, long expiredInSeconds) {
        Instant expire = issuedDate.plusSeconds(expiredInSeconds);

        String scope = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        var authenticatedUser = (AuthenticatedUser) auth.getPrincipal();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(issuedDate)
                .subject(String.valueOf(authenticatedUser.userId()))
                .claim(ROLES_CLAIM, scope)
                .expiresAt(expire)
                .build();  
        return encodeClaimToJwt(claims);
    }

    public String encodeClaimToJwt(JwtClaimsSet claims) {
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
