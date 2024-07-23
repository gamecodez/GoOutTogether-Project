package dev.nncode.gooutbackend.tour;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import dev.nncode.gooutbackend.common.enumeration.TourCompanyStatus;
import dev.nncode.gooutbackend.common.enumeration.TourStatus;
import dev.nncode.gooutbackend.common.exception.EntityNotFound;
import dev.nncode.gooutbackend.tour.dto.TourDto;
import dev.nncode.gooutbackend.tour.model.Tour;
import dev.nncode.gooutbackend.tour.model.TourCount;
import dev.nncode.gooutbackend.tour.repository.TourCountRepository;
import dev.nncode.gooutbackend.tour.repository.TourRepository;
import dev.nncode.gooutbackend.tourcompany.model.TourCompany;
import dev.nncode.gooutbackend.tourcompany.repository.TourCompanyRepository;

@ExtendWith(MockitoExtension.class)
public class TourServiceTest {

    @InjectMocks
    private TourServiceImpl tourService;
    @Mock
    private TourRepository tourRepository;
    @Mock
    private TourCompanyRepository tourCompanyRepository;
    @Mock
    private TourCountRepository tourCountRepository;

    @Test
    void whenCreateTourThenReturnSuccess() {

        var activityDate = Instant.now().plus(Duration.ofDays(5));
        var payload = new TourDto(
                1,
                "Camping",
                "Campaign 3 days 2 night",
                "Forest",
                10,
                activityDate,
                TourStatus.PENDING.name());

        var mockTourCompany = new TourCompany(
                1,
                "Game Tour",
                TourCompanyStatus.WAITING.name());

        when(tourCompanyRepository.findById(payload.tourCompanyId()))
                .thenReturn(Optional.of(mockTourCompany));

        var tour = new Tour(
                1,
                AggregateReference.to(mockTourCompany.id()),
                "Camping",
                "Campaign 3 days 2 night",
                "Forest",
                10,
                activityDate,
                TourStatus.PENDING.name());
        when(tourRepository.save(any(Tour.class)))
                .thenReturn(tour);

        var mockTourCount = new TourCount(1, AggregateReference.to(1), 0);
        when(tourCountRepository.save(any(TourCount.class)))
                .thenReturn(mockTourCount);

        var actual = tourService.createTour(payload);

        assertNotNull(actual);
        assertEquals(tour.id(), actual.id());
        assertEquals(tour.tourCompanyId().getId(), actual.tourCompanyId().getId());
        assertEquals(tour.title(), actual.title());
        assertEquals(tour.description(), actual.description());
        assertEquals(tour.location(), actual.location());
        assertEquals(tour.numberOfPeople(), actual.numberOfPeople());
        assertEquals(tour.activityDate(), actual.activityDate());
        assertEquals(tour.status(), actual.status());
    }

    @Test
    void whenCreateTourButCompanyNotFoundThenReturnNotFound() {
        var payload = new TourDto(
                1,
                "Camping",
                "Campaign 3 days 2 night",
                "Forest",
                10,
                Instant.now().plus(Duration.ofDays(5)),
                TourStatus.PENDING.name());

        when(tourCompanyRepository.findById(any()))
                .thenThrow(new EntityNotFound(String.format("Tour Company with id %s not found", 1)));
        assertThrows(EntityNotFound.class, () -> tourService.createTour(payload));
    }

    @Test
    void whenGetTourByIdThenReturnSuccess() {
        var tour = new Tour(
                1,
                AggregateReference.to(1),
                "Camping",
                "Campaign 3 days 2 night",
                "Forest",
                10,
                Instant.now().plus(Duration.ofDays(5)),
                TourStatus.PENDING.name());
        when(tourRepository.findById(anyInt()))
                .thenReturn(Optional.of(tour));

        var actual = tourService.getTourById(1);
       
        assertNotNull(actual);
        assertEquals(tour.id(), actual.id());
        assertEquals(tour.tourCompanyId().getId(), actual.tourCompanyId().getId());
        assertEquals(tour.title(), actual.title());
        assertEquals(tour.description(), actual.description());
        assertEquals(tour.location(), actual.location());
        assertEquals(tour.numberOfPeople(), actual.numberOfPeople());
        assertEquals(tour.activityDate(), actual.activityDate());
        assertEquals(tour.status(), actual.status());

    }

    @Test
    void whenGetTourByIdNotFoundThenReturnNotFound() {
        when(tourRepository.findById(anyInt()))
                .thenThrow(new EntityNotFound(String.format("Tour with id %s not found", 1)));
        assertThrows(EntityNotFound.class, () -> tourService.getTourById(1));
    }

    @Test
    void whenGetPageTourThenReturnSuccess() {
        List<Tour> tourList = List.of();
        Page<Tour> tourPage = new PageImpl<>(tourList);


        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(0, 5, sort);
        when(tourRepository.findAll(pageable))
                .thenReturn(tourPage);

        var actual = tourService.getPageTour(pageable);
        assertTrue(actual.getContent().isEmpty());
    }
}
