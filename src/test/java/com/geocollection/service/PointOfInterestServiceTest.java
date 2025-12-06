package com.geocollection.service;

import com.geocollection.dto.CreatePoiRequest;
import com.geocollection.dto.PointOfInterestDTO;
import com.geocollection.dto.UpdatePoiRequest;
import com.geocollection.entity.PointOfInterest;
import com.geocollection.exception.ResourceNotFoundException;
import com.geocollection.repository.PointOfInterestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PointOfInterestService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Point of Interest Service Tests")
class PointOfInterestServiceTest {

    @Mock
    private PointOfInterestRepository repository;

    @InjectMocks
    private PointOfInterestService service;

    private PointOfInterest samplePoi;
    private CreatePoiRequest createRequest;
    private UpdatePoiRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Sample POI entity
        samplePoi = new PointOfInterest();
        samplePoi.setId(1L);
        samplePoi.setTitle("Sample POI");
        samplePoi.setDescription("Sample description");
        samplePoi.setLatitude(new BigDecimal("-23.5505"));
        samplePoi.setLongitude(new BigDecimal("-46.6333"));
        samplePoi.setImageBase64(createValidBase64Image());
        samplePoi.setCreatedAt(LocalDateTime.now());
        samplePoi.setUpdatedAt(LocalDateTime.now());

        // Create request
        createRequest = new CreatePoiRequest();
        createRequest.setTitle("New POI");
        createRequest.setDescription("New description");
        createRequest.setLatitude(new BigDecimal("-23.5505"));
        createRequest.setLongitude(new BigDecimal("-46.6333"));
        createRequest.setImageBase64(createValidBase64Image());

        // Update request
        updateRequest = new UpdatePoiRequest();
        updateRequest.setTitle("Updated POI");
        updateRequest.setDescription("Updated description");
        updateRequest.setLatitude(new BigDecimal("-23.5600"));
        updateRequest.setLongitude(new BigDecimal("-46.6400"));
        updateRequest.setImageBase64(createValidBase64Image());
    }

    @Test
    @DisplayName("Should create POI successfully")
    void testCreatePoi_Success() {
        // Given
        when(repository.save(any(PointOfInterest.class))).thenReturn(samplePoi);

        // When
        PointOfInterestDTO result = service.createPoi(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Sample POI");
        verify(repository, times(1)).save(any(PointOfInterest.class));
    }

    @Test
    @DisplayName("Should create POI without image")
    void testCreatePoi_WithoutImage() {
        // Given
        createRequest.setImageBase64(null);
        samplePoi.setImageBase64(null);
        when(repository.save(any(PointOfInterest.class))).thenReturn(samplePoi);

        // When
        PointOfInterestDTO result = service.createPoi(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getImageBase64()).isNull();
        verify(repository, times(1)).save(any(PointOfInterest.class));
    }

    @Test
    @DisplayName("Should throw exception when creating POI with invalid image format")
    void testCreatePoi_InvalidImageFormat() {
        // Given
        createRequest.setImageBase64("data:image/png;base64,invalid_base64_content");

        // When/Then
        assertThatThrownBy(() -> service.createPoi(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid");
    }

    @Test
    @DisplayName("Should throw exception when creating POI with oversized image")
    void testCreatePoi_OversizedImage() {
        // Given - Create a large Base64 string (>5MB)
        byte[] largeImage = new byte[6 * 1024 * 1024]; // 6MB
        String largeBase64 = Base64.getEncoder().encodeToString(largeImage);
        createRequest.setImageBase64("data:image/png;base64," + largeBase64);

        // When/Then
        assertThatThrownBy(() -> service.createPoi(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds maximum");
    }

    @Test
    @DisplayName("Should get all POIs successfully")
    void testGetAllPois_Success() {
        // Given
        PointOfInterest poi2 = new PointOfInterest();
        poi2.setId(2L);
        poi2.setTitle("Second POI");
        poi2.setLatitude(new BigDecimal("-23.5600"));
        poi2.setLongitude(new BigDecimal("-46.6400"));
        poi2.setCreatedAt(LocalDateTime.now());
        poi2.setUpdatedAt(LocalDateTime.now());

        when(repository.findAll()).thenReturn(Arrays.asList(samplePoi, poi2));

        // When
        List<PointOfInterestDTO> result = service.getAllPois();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Sample POI");
        assertThat(result.get(1).getTitle()).isEqualTo("Second POI");
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no POIs exist")
    void testGetAllPois_EmptyList() {
        // Given
        when(repository.findAll()).thenReturn(Arrays.asList());

        // When
        List<PointOfInterestDTO> result = service.getAllPois();

        // Then
        assertThat(result).isEmpty();
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get POI by ID successfully")
    void testGetPoiById_Success() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(samplePoi));

        // When
        PointOfInterestDTO result = service.getPoiById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Sample POI");
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when POI not found by ID")
    void testGetPoiById_NotFound() {
        // Given
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> service.getPoiById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Point of Interest not found with id: 999");
        verify(repository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should update POI successfully")
    void testUpdatePoi_Success() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(samplePoi));
        when(repository.save(any(PointOfInterest.class))).thenReturn(samplePoi);

        // When
        PointOfInterestDTO result = service.updatePoi(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(PointOfInterest.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent POI")
    void testUpdatePoi_NotFound() {
        // Given
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> service.updatePoi(999L, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Point of Interest not found with id: 999");
        verify(repository, times(1)).findById(999L);
        verify(repository, never()).save(any(PointOfInterest.class));
    }

    @Test
    @DisplayName("Should update POI without changing image")
    void testUpdatePoi_WithoutImage() {
        // Given
        updateRequest.setImageBase64(null);
        when(repository.findById(1L)).thenReturn(Optional.of(samplePoi));
        when(repository.save(any(PointOfInterest.class))).thenReturn(samplePoi);

        // When
        PointOfInterestDTO result = service.updatePoi(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(repository, times(1)).save(any(PointOfInterest.class));
    }

    @Test
    @DisplayName("Should delete POI successfully")
    void testDeletePoi_Success() {
        // Given
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);

        // When
        service.deletePoi(1L);

        // Then
        verify(repository, times(1)).existsById(1L);
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent POI")
    void testDeletePoi_NotFound() {
        // Given
        when(repository.existsById(999L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> service.deletePoi(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Point of Interest not found with id: 999");
        verify(repository, times(1)).existsById(999L);
        verify(repository, never()).deleteById(any());
    }

    /**
     * Helper method to create a valid Base64 PNG image.
     */
    private String createValidBase64Image() {
        // Minimal valid PNG header (89 50 4E 47 0D 0A 1A 0A)
        byte[] pngHeader = new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52
        };
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(pngHeader);
    }
}
