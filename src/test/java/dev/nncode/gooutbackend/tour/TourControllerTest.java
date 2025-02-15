package dev.nncode.gooutbackend.tour;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.exception.InternalServerErrorException;

import dev.nncode.gooutbackend.common.enumeration.TourStatus;
import dev.nncode.gooutbackend.common.exception.EntityNotFoundException;
import dev.nncode.gooutbackend.tour.dto.TourDto;
import dev.nncode.gooutbackend.tour.model.Tour;
import dev.nncode.gooutbackend.tour.service.TourService;

@WebMvcTest(TourController.class)
class TourControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TourService tourService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    void whenGetPageTourThenSuccessful() throws Exception {
        var tour = new Tour(
                1,
                AggregateReference.to(1),
                "Camping",
                "Campaign 3 days 2 night",
                "Forest",
                10,
                Instant.now().plus(Duration.ofDays(5)),
                TourStatus.PENDING.name());
        List<Tour> tourList = List.of(tour);
        Page<Tour> pageTours = new PageImpl<>(tourList);
        when(tourService.getPageTour(any(Pageable.class)))
                .thenReturn(pageTours);

        mockMvc.perform(
                get("/api/v1/tours?page=0&size=10&sortField=id&sortDirection=asc")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

    }

    @Test
    void whenGetPageTourButForgotRequiredQueryString() throws Exception {
        mockMvc.perform(
                get("/api/v1/tours?sortField=id&sortDirection=asc")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenCreateTourThenSuccessful() throws Exception {
        var activityDate = Instant.now().plus(Duration.ofDays(5));
        var payload = new TourDto(
                1,
                "Camping",
                "Campaign 3 days 2 night",
                "Forest",
                10,
                activityDate,
                TourStatus.PENDING.name());
        var tour = new Tour(
                1,
                AggregateReference.to(1),
                "Camping",
                "Campaign 3 days 2 night",
                "Forest",
                10,
                activityDate,
                TourStatus.PENDING.name());

        when(tourService.createTour(payload))
                .thenReturn(tour);

        mockMvc.perform(
                post("/api/v1/tours")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void whenCreateTourButMissingSomeFieldsThen400() throws Exception {
        var activityDate = Instant.now().plus(Duration.ofDays(5));
        var payload = new TourDto(
                1,
                null,
                null,
                "Forest",
                10,
                activityDate,
                TourStatus.PENDING.name());
        mockMvc.perform(post("/api/v1/tours")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGetTourByIdThenSuccessful() throws Exception {
        var tour = new Tour(
                1,
                AggregateReference.to(1),
                "Camping",
                "Campaign 3 days 2 night",
                "Forest",
                10,
                Instant.now().plus(Duration.ofDays(5)),
                TourStatus.PENDING.name());

        when(tourService.getTourById(anyInt()))
                .thenReturn(tour);
        mockMvc.perform(
                get(String.format("/api/v1/tours/%d", 1))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void whenGetTourByIdButTourIdNotFoundThenReturn404() throws Exception {

        when(tourService.getTourById(anyInt()))
                .thenThrow(new EntityNotFoundException());

        mockMvc.perform(get(String.format("/api/v1/tours/%d", 1)))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGetTourByIdButServerErrorThenReturn500() throws Exception {

        when(tourService.getTourById(anyInt()))
                .thenThrow(new InternalServerErrorException("Mock error"));

        mockMvc.perform(get(String.format("/api/v1/tours/%d", 1)))
                .andExpect(status().isInternalServerError());
    }

}
