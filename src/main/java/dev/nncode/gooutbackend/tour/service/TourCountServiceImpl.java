package dev.nncode.gooutbackend.tour.service;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

import dev.nncode.gooutbackend.common.exception.EntityNotFoundException;
import dev.nncode.gooutbackend.tour.model.TourCount;
import dev.nncode.gooutbackend.tour.repository.TourCountRepository;

@Service
public class TourCountServiceImpl implements TourCountService {

    private final TourCountRepository tourCountRepository;

    public TourCountServiceImpl(TourCountRepository tourCountRepository) {
        this.tourCountRepository = tourCountRepository;
    }
    @Override
    public void incrementTourCount(int tourId) {
        var tourCount = tourCountRepository.findOneByTourId(AggregateReference.to(tourId))
                .orElseThrow(() -> new EntityNotFoundException(String.format("TourCount for tourId %s not found", tourId)));
        var newAmount = tourCount.amount() + 1;
        var prepareTourCount = new TourCount(tourCount.id(), tourCount.tourId(), newAmount);
        tourCountRepository.save(prepareTourCount);
    }

    @Override
    public void decrementTourCount(int tourId) {
        var tourCount = tourCountRepository.findOneByTourId(AggregateReference.to(tourId))
                .orElseThrow(() -> new EntityNotFoundException(String.format("TourCount for tourId %s not found", tourId)));
        var newAmount = tourCount.amount() - 1;
        var prepareTourCount = new TourCount(tourCount.id(), tourCount.tourId(), newAmount);
        tourCountRepository.save(prepareTourCount);
    }

}
