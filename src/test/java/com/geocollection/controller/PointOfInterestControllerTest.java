package com.geocollection.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geocollection.dto.CreatePoiRequest;
import com.geocollection.dto.PointOfInterestDTO;
import com.geocollection.dto.UpdatePoiRequest;
import com.geocollection.exception.ResourceNotFoundException;
import com.geocollection.service.PointOfInterestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PointOfInterestController.
 */
@WebMvcTest(PointOfInterestController.class)
@DisplayName("Point of Interest Controller Tests")
class PointOfInterestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PointOfInterestService service;

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
                LocalDateTime.now(),
                LocalDateTime.now());

        createRequest = new CreatePoiRequest();
        createRequest.setTitle("New POI");
        createRequest.setDescription("New description");
        createRequest.setLatitude(new BigDecimal("-23.5505"));
        createRequest.setLongitude(new BigDecimal("-46.6333"));
        createRequest.setImageBase64("base64imagedata");

        updateRequest = new UpdatePoiRequest();
        updateRequest.setTitle("Updated POI");
        updateRequest.setDescription("Updated description");
        updateRequest.setLatitude(new BigDecimal("-23.5600"));
        updateRequest.setLongitude(new BigDecimal("-46.6400"));
        updateRequest.setImageBase64("base64imagedata");
    }

    @Test
    @DisplayName("POST /api/pois - Should create POI successfully")
    void testCreatePoi_Success() throws Exception {
        // Given
        when(service.createPoi(any(CreatePoiRequest.class))).thenReturn(sampleDTO);

        // When/Then
        mockMvc.perform(post("/api/pois")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Sample POI"))
                .andExpect(jsonPath("$.latitude").value(-23.5505))
                .andExpect(jsonPath("$.longitude").value(-46.6333));

        verify(service, times(1)).createPoi(any(CreatePoiRequest.class));
    }

    @Test
    @DisplayName("POST /api/pois - Should return 400 when title is blank")
    void testCreatePoi_BlankTitle() throws Exception {
        // Given
        createRequest.setTitle("");

        // When/Then
        mockMvc.perform(post("/api/pois")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(service, never()).createPoi(any(CreatePoiRequest.class));
    }

    @Test
    @DisplayName("POST /api/pois - Should return 400 when latitude is null")
    void testCreatePoi_NullLatitude() throws Exception {
        // Given
        createRequest.setLatitude(null);

        // When/Then
        mockMvc.perform(post("/api/pois")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(service, never()).createPoi(any(CreatePoiRequest.class));
    }

    @Test
    @DisplayName("POST /api/pois - Should return 400 when latitude is out of range")
    void testCreatePoi_InvalidLatitude() throws Exception {
        // Given
        createRequest.setLatitude(new BigDecimal("95.0"));

        // When/Then
        mockMvc.perform(post("/api/pois")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(service, never()).createPoi(any(CreatePoiRequest.class));
    }

    @Test
    @DisplayName("GET /api/pois - Should return all POIs")
    void testGetAllPois_Success() throws Exception {
        // Given
        PointOfInterestDTO dto2 = new PointOfInterestDTO(
                2L, "Second POI", "Second description",
                new BigDecimal("-23.5600"), new BigDecimal("-46.6400"),
                null, LocalDateTime.now(), LocalDateTime.now());
        List<PointOfInterestDTO> pois = Arrays.asList(sampleDTO, dto2);
        when(service.getAllPois()).thenReturn(pois);

        // When/Then
        mockMvc.perform(get("/api/pois"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Sample POI"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Second POI"));

        verify(service, times(1)).getAllPois();
    }

    @Test
    @DisplayName("GET /api/pois - Should return empty list")
    void testGetAllPois_EmptyList() throws Exception {
        // Given
        when(service.getAllPois()).thenReturn(Arrays.asList());

        // When/Then
        mockMvc.perform(get("/api/pois"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(service, times(1)).getAllPois();
    }

    @Test
    @DisplayName("GET /api/pois/{id} - Should return POI by ID")
    void testGetPoiById_Success() throws Exception {
        // Given
        when(service.getPoiById(1L)).thenReturn(sampleDTO);

        // When/Then
        mockMvc.perform(get("/api/pois/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Sample POI"))
                .andExpect(jsonPath("$.description").value("Sample description"));

        verify(service, times(1)).getPoiById(1L);
    }

    @Test
    @DisplayName("GET /api/pois/{id} - Should return 404 when POI not found")
    void testGetPoiById_NotFound() throws Exception {
        // Given
        when(service.getPoiById(999L)).thenThrow(new ResourceNotFoundException("Point of Interest", 999L));

        // When/Then
        mockMvc.perform(get("/api/pois/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));

        verify(service, times(1)).getPoiById(999L);
    }

    @Test
    @DisplayName("PUT /api/pois/{id} - Should update POI successfully")
    void testUpdatePoi_Success() throws Exception {
        // Given
        PointOfInterestDTO updatedDTO = new PointOfInterestDTO(
                1L, "Updated POI", "Updated description",
                new BigDecimal("-23.5600"), new BigDecimal("-46.6400"),
                "base64imagedata", LocalDateTime.now(), LocalDateTime.now());
        when(service.updatePoi(eq(1L), any(UpdatePoiRequest.class))).thenReturn(updatedDTO);

        // When/Then
        mockMvc.perform(put("/api/pois/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated POI"))
                .andExpect(jsonPath("$.latitude").value(-23.5600));

        verify(service, times(1)).updatePoi(eq(1L), any(UpdatePoiRequest.class));
    }

    @Test
    @DisplayName("PUT /api/pois/{id} - Should return 404 when updating non-existent POI")
    void testUpdatePoi_NotFound() throws Exception {
        // Given
        when(service.updatePoi(eq(999L), any(UpdatePoiRequest.class)))
                .thenThrow(new ResourceNotFoundException("Point of Interest", 999L));

        // When/Then
        mockMvc.perform(put("/api/pois/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(service, times(1)).updatePoi(eq(999L), any(UpdatePoiRequest.class));
    }

    @Test
    @DisplayName("PUT /api/pois/{id} - Should return 400 when update data is invalid")
    void testUpdatePoi_InvalidData() throws Exception {
        // Given
        updateRequest.setTitle("");

        // When/Then
        mockMvc.perform(put("/api/pois/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(service, never()).updatePoi(any(), any());
    }

    @Test
    @DisplayName("DELETE /api/pois/{id} - Should delete POI successfully")
    void testDeletePoi_Success() throws Exception {
        // Given
        doNothing().when(service).deletePoi(1L);

        // When/Then
        mockMvc.perform(delete("/api/pois/1"))
                .andExpect(status().isNoContent());

        verify(service, times(1)).deletePoi(1L);
    }

    @Test
    @DisplayName("DELETE /api/pois/{id} - Should return 404 when deleting non-existent POI")
    void testDeletePoi_NotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Point of Interest", 999L))
                .when(service).deletePoi(999L);

        // When/Then
        mockMvc.perform(delete("/api/pois/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(service, times(1)).deletePoi(999L);
    }
}
