package com.geocollection.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geocollection.dto.CreatePoiRequest;
import com.geocollection.dto.PointOfInterestDTO;
import com.geocollection.dto.UpdatePoiRequest;
import com.geocollection.service.PointOfInterestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PointOfInterestController.class)
@Import(com.geocollection.config.TestSecurityConfig.class)
@DisplayName("Point of Interest Controller Tests")
class PointOfInterestControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private PointOfInterestService service;

        @MockBean
        private com.geocollection.security.JwtUtil jwtUtil;

        @MockBean
        private com.geocollection.security.JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockBean
        private com.geocollection.security.CustomUserDetailsService userDetailsService;

        private PointOfInterestDTO sampleDTO;
        private CreatePoiRequest createRequest;
        private UpdatePoiRequest updateRequest;

        @BeforeEach
        void setUp() {
                sampleDTO = new PointOfInterestDTO(
                                1L,
                                "Sample POI",
                                "Sample description",
                                new BigDecimal("-23.5505"),
                                new BigDecimal("-46.6333"),
                                "base64imagedata",
                                false,
                                null,
                                LocalDateTime.now(),
                                LocalDateTime.now());

                createRequest = new CreatePoiRequest();
                createRequest.setTitle("New POI");
                createRequest.setDescription("New description");
                createRequest.setLatitude(new BigDecimal("-23.5505"));
                createRequest.setLongitude(new BigDecimal("-46.6333"));
                createRequest.setImageBase64("base64imagedata");
                createRequest.setIsHome(false);

                updateRequest = new UpdatePoiRequest();
                updateRequest.setTitle("Updated POI");
                updateRequest.setDescription("Updated description");
                updateRequest.setLatitude(new BigDecimal("-23.5600"));
                updateRequest.setLongitude(new BigDecimal("-46.6400"));
                updateRequest.setImageBase64("base64imagedata");
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("POST /api/pois - Should create POI successfully")
        void testCreatePoi_Success() throws Exception {
                when(service.createPoi(any(CreatePoiRequest.class), eq("testuser"))).thenReturn(sampleDTO);

                mockMvc.perform(post("/api/pois")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.title").value("Sample POI"));

                verify(service, times(1)).createPoi(any(CreatePoiRequest.class), eq("testuser"));
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("GET /api/pois - Should return all POIs for user")
        void testGetAllPois_Success() throws Exception {
                PointOfInterestDTO dto2 = new PointOfInterestDTO(
                                2L, "Second POI", "Second description",
                                new BigDecimal("-23.5600"), new BigDecimal("-46.6400"),
                                null, true, 5.2, LocalDateTime.now(), LocalDateTime.now());
                List<PointOfInterestDTO> pois = Arrays.asList(sampleDTO, dto2);
                when(service.getAllPois("testuser")).thenReturn(pois);

                mockMvc.perform(get("/api/pois"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(2)))
                                .andExpect(jsonPath("$[1].isHome").value(true))
                                .andExpect(jsonPath("$[1].distanceFromHome").value(5.2));

                verify(service, times(1)).getAllPois("testuser");
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("DELETE /api/pois/{id} - Should delete POI successfully")
        void testDeletePoi_Success() throws Exception {
                doNothing().when(service).deletePoi(1L, "testuser");

                mockMvc.perform(delete("/api/pois/1")
                                .with(csrf()))
                                .andExpect(status().isNoContent());

                verify(service, times(1)).deletePoi(1L, "testuser");
        }

        @Test
        @DisplayName("POST /api/pois - Should return 401 when not authenticated")
        void testCreatePoi_Unauthorized() throws Exception {
                mockMvc.perform(post("/api/pois")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isUnauthorized());

                verify(service, never()).createPoi(any(), anyString());
        }
}
