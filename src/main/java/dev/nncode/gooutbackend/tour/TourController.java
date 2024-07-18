package dev.nncode.gooutbackend.tour;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;




@RestController
@RequestMapping("/tours")
public class TourController {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(1);

    private final Logger logger = LoggerFactory.getLogger(TourController.class);
    private final Map<Integer, Tour> tourInMemDb;

    public TourController() {
        tourInMemDb = new HashMap<>();
    }

    // CRUD - create, read, update, delete


    @GetMapping
    public List<Tour> getTours() {
        logger.info("Get all tours");
        return tourInMemDb.entrySet().stream()
                .map(Map.Entry::getValue)
                .toList();
    }

    @GetMapping("/{id}")
    public Tour getTourById(@PathVariable int id) {
        logger.info("Get tourId: {}", id);
        return Optional.ofNullable(tourInMemDb.get(id))
                .orElseThrow(() -> {
                    logger.error("tourId: {} not found", id);
                    return new RuntimeException("No tour with id " + id);
                });
    }

    @PostMapping    
    @ResponseStatus(HttpStatus.CREATED)
    public Tour createTour(@RequestBody Tour tour) {
        var newTour = new Tour(
            ATOMIC_INTEGER.getAndIncrement(), 
            tour.title(), 
            tour.maxPeople()
        );
        var id = newTour.id();
        tourInMemDb.put(id, newTour);
        logger.info("Create new tour: {}", tourInMemDb.get(id));
        return tourInMemDb.get(id);
    }

    @PutMapping("/{id}")
    public Tour updateTour(@PathVariable int id, @RequestBody Tour tour) {
        var updatedTour = new Tour(
            id, 
            tour.title(), 
            tour.maxPeople()
        );
        tourInMemDb.put(id, updatedTour);
        logger.info("Updated tour: {}", tourInMemDb.get(id));
        return tourInMemDb.get(id);
    }

    @DeleteMapping("/{id}")
    public String deleteTour(@PathVariable int id) {
        if (!tourInMemDb.containsKey(id)) {
            logger.error("tourId: {} not found", id);
            return "Failed to delete tour with id " + id;
        }
        tourInMemDb.remove(id);
        logger.info("Deleted tourId: {} success", id);
        return "Success to delete tour with id " + id;
    }
}
