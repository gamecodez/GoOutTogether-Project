package dev.nncode.gooutbackend.tourcompany;

import java.math.BigDecimal;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.nncode.gooutbackend.common.enumeration.TourCompanyStatus;
import dev.nncode.gooutbackend.common.exception.EntityNotFound;
import dev.nncode.gooutbackend.tourcompany.dto.RegisterTourCompanyDto;
import dev.nncode.gooutbackend.tourcompany.model.TourCompany;
import dev.nncode.gooutbackend.tourcompany.model.TourCompanyLogin;
import dev.nncode.gooutbackend.tourcompany.model.TourCompanyWallet;
import dev.nncode.gooutbackend.tourcompany.repository.TourCompanyLoginRepository;
import dev.nncode.gooutbackend.tourcompany.repository.TourCompanyRepository;
import dev.nncode.gooutbackend.tourcompany.repository.TourCompanyWalletRepository;

@Service
public class TourCompanyServiceImpl implements TourCompanyService {

    private final Logger logger = LoggerFactory.getLogger(TourCompanyServiceImpl.class);

    private final TourCompanyRepository tourCompanyRepository;
    private final TourCompanyLoginRepository tourCompanyLoginRepository;
    private final TourCompanyWalletRepository tourCompanyWalletRepository;
    private final PasswordEncoder passwordEncoder;

    public TourCompanyServiceImpl(TourCompanyRepository tourCompanyRepository, TourCompanyLoginRepository tourCompanyLoginRepository, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder, dev.nncode.gooutbackend.tourcompany.repository.TourCompanyWalletRepository tourCompanyWalletRepository) {
        this.tourCompanyRepository = tourCompanyRepository;
        this.tourCompanyLoginRepository = tourCompanyLoginRepository;
        this.passwordEncoder = passwordEncoder;
        this.tourCompanyWalletRepository = tourCompanyWalletRepository;
    }

    @Override
    @Transactional
    public TourCompany registerTourCompany(RegisterTourCompanyDto payload) {
        logger.debug("[registerTour] newly tour comapany is registering...");
        var companyName = payload.name();
        var tourCompany = new TourCompany(null, companyName , TourCompanyStatus.WAITING.name());
        var newTourCompany = tourCompanyRepository.save(tourCompany);
        logger.debug("[registerTour] new created tour company: {}", newTourCompany);
        createTourCompanyCredentials(newTourCompany, payload);
        return newTourCompany;
    }

    @Override
    @Transactional
    public TourCompany approvedTourCompany(Integer id) {
        var tourCompany = tourCompanyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFound(String.format("Tour Company with id %s not found", id)));
        tourCompany = new TourCompany(id, tourCompany.name(), TourCompanyStatus.APPROVED.name());
        var updatedTourCompany = tourCompanyRepository.save(tourCompany);
        createTourCompanyWallet(updatedTourCompany);
        return updatedTourCompany;
    }


    private void createTourCompanyCredentials(TourCompany tourCompany, RegisterTourCompanyDto payload) {
        AggregateReference<TourCompany, Integer> tourCompanyReference = AggregateReference.to(tourCompany.id());
        var encryptedPassword = passwordEncoder.encode(payload.password());
        var companyCredential = new TourCompanyLogin(null, tourCompanyReference, payload.username(), encryptedPassword);
        tourCompanyLoginRepository.save(companyCredential);
        logger.info("[createTourCompanyCredentials] new created tour company credentials: {}", tourCompany.id());
    }

    private void createTourCompanyWallet(TourCompany tourCompany) {
        AggregateReference<TourCompany, Integer> tourCompanyReference = AggregateReference.to(tourCompany.id());
        Instant currentTimestamp = Instant.now();
        BigDecimal initBalance = new BigDecimal(0.00);
        var wallet = new TourCompanyWallet(null, tourCompanyReference, currentTimestamp, initBalance);
        tourCompanyWalletRepository.save(wallet);
        logger.info("[createTourCompanyWallet] new created tour company wallet: {}", tourCompany.id());
    }

}
