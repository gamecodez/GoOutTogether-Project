package dev.nncode.gooutbackend.tourcompany;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.crypto.password.PasswordEncoder;

import dev.nncode.gooutbackend.common.enumeration.TourCompanyStatus;
import dev.nncode.gooutbackend.common.exception.EntityNotFoundException;
import dev.nncode.gooutbackend.tourcompany.dto.RegisterTourCompanyDto;
import dev.nncode.gooutbackend.tourcompany.model.TourCompany;
import dev.nncode.gooutbackend.tourcompany.model.TourCompanyLogin;
import dev.nncode.gooutbackend.tourcompany.repository.TourCompanyLoginRepository;
import dev.nncode.gooutbackend.tourcompany.repository.TourCompanyRepository;
import dev.nncode.gooutbackend.wallet.model.TourCompanyWallet;
import dev.nncode.gooutbackend.wallet.repository.TourCompanyWalletRepository;

@ExtendWith(MockitoExtension.class)
class TourCompanyServiceTest {

    @InjectMocks
    private TourCompanyServiceImpl tourCompanyService;
    @Mock
    private TourCompanyRepository tourCompanyRepository;
    @Mock
    private TourCompanyLoginRepository tourCompanyLoginRepository;
    @Mock
    private TourCompanyWalletRepository tourCompanyWalletRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void whenRegisterTourThenSuccess() {

        var mockTourCompany = new TourCompany(
                1,
                "Game Tour",
                TourCompanyStatus.WAITING.name());
        when(tourCompanyRepository.save(any(TourCompany.class)))
                .thenReturn(mockTourCompany);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encryptedValue");
        var companyCredential = new TourCompanyLogin(1, AggregateReference.to(1), "game", "encryptedValue");
        when(tourCompanyLoginRepository.save(any(TourCompanyLogin.class)))
                .thenReturn(companyCredential);
        var payload = new RegisterTourCompanyDto(
                null,
                "Game Tour",
                "game",
                "123456789",
                null);
        var actual = tourCompanyService.registerTourCompany(payload);
        assertNotNull(actual);
        assertEquals(1, actual.id().intValue());
        assertEquals("Game Tour", actual.name());
        assertEquals(TourCompanyStatus.WAITING.name(), actual.status());
    }

    @Test
    void whenApprovedTourThenSuccess() {
        var mockTourCompany = new TourCompany(
                1,
                "Game Tour",
                TourCompanyStatus.WAITING.name());
        when(tourCompanyRepository.findById(anyInt()))
                .thenReturn(Optional.of(mockTourCompany));

        var updatedTourCompany = new TourCompany(mockTourCompany.id(), mockTourCompany.name(),
                TourCompanyStatus.APPROVED.name());
        when(tourCompanyRepository.save(any(TourCompany.class)))
                .thenReturn(updatedTourCompany);

        var wallet = new TourCompanyWallet(null, AggregateReference.to(1), Instant.now(), new BigDecimal(0.00));
        when(tourCompanyWalletRepository.save(any(TourCompanyWallet.class)))
                .thenReturn(wallet);
        var actual = tourCompanyService.approvedTourCompany(1);

        assertNotNull(actual);
        assertEquals(1, actual.id().intValue());
        assertEquals("Game Tour", actual.name());
        assertEquals(TourCompanyStatus.APPROVED.name(), actual.status());

    }

    @Test
    void whenApprovedTourButTourCompanyNotFoundThenError() {
        when(tourCompanyRepository.findById(anyInt()))
                .thenThrow(new EntityNotFoundException(String.format("Tour Company with id %s not found", 1)));
        assertThrows(EntityNotFoundException.class, () -> tourCompanyService.approvedTourCompany(1));
    }
}
