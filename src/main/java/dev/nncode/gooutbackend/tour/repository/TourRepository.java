package dev.nncode.gooutbackend.tour.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;

import dev.nncode.gooutbackend.tour.model.Tour;

public interface TourRepository extends ListCrudRepository<Tour, Integer> {

    Page<Tour> findAll(Pageable pageable);

}
