package dev.nncode.gooutbackend.tour.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.nncode.gooutbackend.common.enumeration.TourStatus;
import dev.nncode.gooutbackend.common.exception.EntityNotFoundException;
import dev.nncode.gooutbackend.tour.dto.TourDto;
import dev.nncode.gooutbackend.tour.model.Tour;
import dev.nncode.gooutbackend.tour.model.TourCount;
import dev.nncode.gooutbackend.tour.repository.TourCountRepository;
import dev.nncode.gooutbackend.tour.repository.TourRepository;
import dev.nncode.gooutbackend.tourcompany.model.TourCompany;
import dev.nncode.gooutbackend.tourcompany.repository.TourCompanyRepository;

@Service
public class TourServiceImpl implements TourService {

    private final Logger logger = LoggerFactory.getLogger(TourServiceImpl.class);

    private final TourRepository tourRepository;
    private final TourCompanyRepository tourCompanyRepository;
    private final TourCountRepository tourCountRepository;

    public TourServiceImpl(dev.nncode.gooutbackend.tour.repository.TourRepository tourRepository,
            dev.nncode.gooutbackend.tourcompany.repository.TourCompanyRepository tourCompanyRepository,
            dev.nncode.gooutbackend.tour.repository.TourCountRepository tourCountRepository) {
        this.tourRepository = tourRepository;
        this.tourCompanyRepository = tourCompanyRepository;
        this.tourCountRepository = tourCountRepository;
    }

    @Override
    @Transactional
    public Tour createTour(TourDto body) {
        var tourCompanyId = body.tourCompanyId();
        var tourcompany = tourCompanyRepository.findById(tourCompanyId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Tour Company with id %s not found", tourCompanyId)));
        AggregateReference<TourCompany, Integer> tourCompanyReference = AggregateReference.to(tourcompany.id());
        var tour = new Tour(
                null,
                tourCompanyReference,
                body.title(),
                body.description(),
                body.location(),
                body.numberOfPeople(),
                body.activityDate(),
                TourStatus.PENDING.name());
        var newTour = tourRepository.save(tour);
        logger.debug("Tour has been created: {}", tour);
        tourCountRepository.save(new TourCount(null, AggregateReference.to(newTour.id()), 0));
        return newTour;

    }

    @Override
    public Tour getTourById(int id) {
        return tourRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Tour with id %s not found", id)));
    }

    @Override
    public Page<Tour> getPageTour(Pageable pageable) {
        return tourRepository.findAll(pageable);
    }

}
