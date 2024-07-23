package dev.nncode.gooutbackend.tourcompany;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.nncode.gooutbackend.common.enumeration.TourCompanyStatus;
import dev.nncode.gooutbackend.common.exception.EntityNotFound;
import dev.nncode.gooutbackend.tourcompany.dto.RegisterTourCompanyDto;
import dev.nncode.gooutbackend.tourcompany.model.TourCompany;

@WebMvcTest(TourCompanyController.class)
class TourCompanyControllerTest {

        @Autowired
        private WebApplicationContext webApplicationContext;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private TourCompanyService tourCompanyService;

        private MockMvc mockMvc;

        @BeforeEach
        public void setup() {
                mockMvc = MockMvcBuilders
                                .webAppContextSetup(webApplicationContext)
                                .build();
        }

        @Test
        void whenCreateTourCompanyThenSuccessful() throws Exception {
                var mockTourCompany = new TourCompany(
                                1,
                                "Game Tour",
                                TourCompanyStatus.WAITING.name());

                when(tourCompanyService.registerTourCompany(any(RegisterTourCompanyDto.class)))
                                .thenReturn(mockTourCompany);
                var payload = new RegisterTourCompanyDto(
                                null,
                                "Game Tour",
                                "game",
                                "123456789",
                                null);
                mockMvc.perform(
                                post("/api/v1/tour-companies")
                                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                .content(objectMapper.writeValueAsString(payload)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        void whenApprovedTourCompanyThenSuccessful() throws Exception {
                var mockTourCompany = new TourCompany(
                                1,
                                "Game Tour",
                                TourCompanyStatus.APPROVED.name());

                when(tourCompanyService.approvedTourCompany(anyInt()))
                                .thenReturn(mockTourCompany);

                mockMvc.perform(
                                post(String.format("/api/v1/tour-companies/%d/approve", 1))
                                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.status").value(TourCompanyStatus.APPROVED.name()));
        }

        @Test
        void whenApproveTourButCompanyNotFoundThenReturn404() throws Exception {

                when(tourCompanyService.approvedTourCompany(anyInt()))
                                .thenThrow(new EntityNotFound());

                mockMvc.perform(post(String.format("/api/v1/tour-companies/%d/approve", 1)))
                                .andExpect(status().isNotFound());
        }
}
