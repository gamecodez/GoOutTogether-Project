package dev.nncode.gooutbackend.tour;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import dev.nncode.gooutbackend.tour.dto.TourDto;
import dev.nncode.gooutbackend.tour.model.Tour;

public interface TourService {

    Tour createTour(TourDto body);

    Tour getTourById(int id);

    Page<Tour> getPageTour(Pageable pageable);

}
