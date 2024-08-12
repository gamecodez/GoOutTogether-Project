package dev.nncode.gooutbackend.booking;

import java.time.Instant;

import org.jobrunr.scheduling.BackgroundJob;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.nncode.gooutbackend.tour.model.TourCount;
import dev.nncode.gooutbackend.tour.repository.TourCountRepository;

@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {

    private final TourCountRepository tourCountRepository;

    public BookingController(TourCountRepository tourCountRepository) {
        this.tourCountRepository = tourCountRepository;
    }

    @PostMapping
    public ResponseEntity<?> postMethodName(@RequestBody Payload body) {
        BackgroundJob.<SimpleService>schedule(body.time, x -> x.updateTourCount(5));
        BackgroundJob.<SimpleService>schedule(body.time, x -> x.updateTourCount(-3));
        return ResponseEntity.noContent().build();
    }

    @Service
    public class SimpleService {

        @Transactional
        public void updateTourCount(int value) {
            var tourCount = tourCountRepository.findById(1).get();
            // var tourCount = tourCountRepository.findOneByTourId(AggregateReference.to(1)).get();
            var newValue = tourCount.amount() + value;
            var updated = new TourCount(tourCount.id(), tourCount.tourId(), newValue);
            tourCountRepository.save(updated);
        }
    }

    public static record Payload(Instant time) {
    }

}
