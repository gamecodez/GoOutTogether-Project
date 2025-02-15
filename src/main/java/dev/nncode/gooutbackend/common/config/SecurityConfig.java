package dev.nncode.gooutbackend.common.config;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import dev.nncode.gooutbackend.common.enumeration.RoleEnum;
import dev.nncode.gooutbackend.common.model.RSAKeyProperties;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String privateKeyBase64;
    private final String publicKeyBase64;

    public SecurityConfig(
            @Value("${oauth.public-key}") String publicKeyBase64,
            @Value("${oauth.private-key}") String privateKeyBase64) {
        this.publicKeyBase64 = publicKeyBase64;
        this.privateKeyBase64 = privateKeyBase64;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Actuator
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/metrics").permitAll()
                        // Auth
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/auth/refresh").permitAll()
                        // Tour Company
                        .requestMatchers(HttpMethod.POST, "/api/v1/tour-company").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/tour-company/{id:\\d+}")
                        .hasRole(RoleEnum.ADMIN.name())
                        // Tour
                        .requestMatchers(HttpMethod.GET, "/api/v1/tours").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/tours/{id:\\d+}").permitAll()
                        // User
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                        .requestMatchers("/api/v1/users/**").hasRole(RoleEnum.ADMIN.name())
                        // Wallet
                        .requestMatchers(HttpMethod.GET, "/api/v1/wallets/me").hasRole(RoleEnum.CONSUMER.name())
                        .requestMatchers(HttpMethod.POST, "/api/v1/wallets/topup").hasRole(RoleEnum.CONSUMER.name())
                        // Payment
                        .requestMatchers(HttpMethod.GET, "/api/v1/payment/**").hasRole(RoleEnum.CONSUMER.name())
                        // Booking
                        .requestMatchers(HttpMethod.POST, "/api/v1/booking/**").hasRole(RoleEnum.CONSUMER.name())
                        // User self-managed (only for self-managed user)
                        .requestMatchers("/api/v1/me").hasRole(RoleEnum.CONSUMER.name())
                        // Administrrator purpose
                        .requestMatchers("/api/v1/admin/**").hasRole(RoleEnum.ADMIN.name())
                        .anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(rs -> rs.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtConverter;
    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder,
            UserDetailsService userDetailsService) {
        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
        daoProvider.setPasswordEncoder(passwordEncoder);
        daoProvider.setUserDetailsService(userDetailsService);
        return new ProviderManager(daoProvider);
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        return new InMemoryUserDetailsManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAKeyProperties rsaInstance) {
        JWK jwt = new RSAKey.Builder(rsaInstance.publicKey()).privateKey(rsaInstance.privateKey()).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwt));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAKeyProperties rsaInstance) {
        return NimbusJwtDecoder.withPublicKey(rsaInstance.publicKey()).build();
    }

    @Bean
    public RSAKeyProperties rsaInstance() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        // Resource privateKeyPkcs8 =
        // resourceLoader.getResource("classPath:private_key_pkcs8.pem");
        String privateKeyContent = new String(Base64.decodeBase64(privateKeyBase64));
        // Resource publicKey = resourceLoader.getResource("classPath:public_key.pem");
        String publicKeyContent = new String(Base64.decodeBase64(publicKeyBase64));
        privateKeyContent = privateKeyContent.replaceAll("\\n", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "");
        publicKeyContent = publicKeyContent.replaceAll("\\n", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "");
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyContent));
        PrivateKey privKey = kf.generatePrivate(keySpecPKCS8);
        X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.decodeBase64(publicKeyContent));
        RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(keySpecX509);
        return new RSAKeyProperties(pubKey, privKey);
    }
}
