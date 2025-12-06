package com.geocollection.service;

import com.geocollection.dto.CreatePoiRequest;
import com.geocollection.dto.PointOfInterestDTO;
import com.geocollection.dto.UpdatePoiRequest;
import com.geocollection.entity.PointOfInterest;
import com.geocollection.exception.ResourceNotFoundException;
import com.geocollection.repository.PointOfInterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing Points of Interest.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PointOfInterestService {

    private final PointOfInterestRepository repository;

    private static final int MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB in bytes

    /**
     * Create a new Point of Interest.
     */
    @Transactional
    public PointOfInterestDTO createPoi(CreatePoiRequest request) {
        log.info("Creating new POI: {}", request.getTitle());

        // Validate image if present
        if (request.getImageBase64() != null && !request.getImageBase64().isEmpty()) {
            validateImage(request.getImageBase64());
        }

        PointOfInterest poi = new PointOfInterest();
        poi.setTitle(request.getTitle());
        poi.setDescription(request.getDescription());
        poi.setLatitude(request.getLatitude());
        poi.setLongitude(request.getLongitude());
        poi.setImageBase64(request.getImageBase64());

        PointOfInterest savedPoi = repository.save(poi);
        log.info("POI created successfully with ID: {}", savedPoi.getId());

        return convertToDTO(savedPoi);
    }

    /**
     * Get all Points of Interest.
     */
    @Transactional(readOnly = true)
    public List<PointOfInterestDTO> getAllPois() {
        log.info("Fetching all POIs");
        List<PointOfInterest> pois = repository.findAll();
        log.info("Found {} POIs", pois.size());
        return pois.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get a Point of Interest by ID.
     */
    @Transactional(readOnly = true)
    public PointOfInterestDTO getPoiById(Long id) {
        log.info("Fetching POI with ID: {}", id);
        PointOfInterest poi = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Point of Interest", id));
        return convertToDTO(poi);
    }

    /**
     * Update an existing Point of Interest.
     */
    @Transactional
    public PointOfInterestDTO updatePoi(Long id, UpdatePoiRequest request) {
        log.info("Updating POI with ID: {}", id);

        PointOfInterest poi = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Point of Interest", id));

        // Validate image if present and changed
        if (request.getImageBase64() != null && !request.getImageBase64().isEmpty()) {
            validateImage(request.getImageBase64());
        }

        poi.setTitle(request.getTitle());
        poi.setDescription(request.getDescription());
        poi.setLatitude(request.getLatitude());
        poi.setLongitude(request.getLongitude());
        poi.setImageBase64(request.getImageBase64());

        PointOfInterest updatedPoi = repository.save(poi);
        log.info("POI updated successfully with ID: {}", updatedPoi.getId());

        return convertToDTO(updatedPoi);
    }

    /**
     * Delete a Point of Interest.
     */
    @Transactional
    public void deletePoi(Long id) {
        log.info("Deleting POI with ID: {}", id);

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Point of Interest", id);
        }

        repository.deleteById(id);
        log.info("POI deleted successfully with ID: {}", id);
    }

    /**
     * Validate Base64 image.
     */
    private void validateImage(String base64Image) {
        try {
            // Remove data URL prefix if present (e.g., "data:image/png;base64,")
            String base64Data = base64Image;
            if (base64Image.contains(",")) {
                base64Data = base64Image.split(",")[1];
            }

            // Decode to check if valid Base64
            byte[] decodedBytes = Base64.getDecoder().decode(base64Data);

            // Check size
            if (decodedBytes.length > MAX_IMAGE_SIZE) {
                throw new IllegalArgumentException(
                        String.format("Image size exceeds maximum allowed size of %d MB",
                                MAX_IMAGE_SIZE / (1024 * 1024)));
            }

            // Check if it's a valid image format (basic check by magic numbers)
            if (!isValidImageFormat(decodedBytes)) {
                throw new IllegalArgumentException("Invalid image format. Only JPEG, PNG, and GIF are supported.");
            }

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Illegal base64 character")) {
                throw new IllegalArgumentException("Invalid Base64 image data");
            }
            throw e;
        }
    }

    /**
     * Check if byte array represents a valid image format.
     */
    private boolean isValidImageFormat(byte[] data) {
        if (data.length < 4) {
            return false;
        }

        // Check for JPEG magic numbers (FF D8 FF)
        if (data[0] == (byte) 0xFF && data[1] == (byte) 0xD8 && data[2] == (byte) 0xFF) {
            return true;
        }

        // Check for PNG magic numbers (89 50 4E 47)
        if (data[0] == (byte) 0x89 && data[1] == (byte) 0x50 &&
                data[2] == (byte) 0x4E && data[3] == (byte) 0x47) {
            return true;
        }

        // Check for GIF magic numbers (47 49 46)
        if (data[0] == (byte) 0x47 && data[1] == (byte) 0x49 && data[2] == (byte) 0x46) {
            return true;
        }

        return false;
    }

    /**
     * Convert entity to DTO.
     */
    private PointOfInterestDTO convertToDTO(PointOfInterest poi) {
        return new PointOfInterestDTO(
                poi.getId(),
                poi.getTitle(),
                poi.getDescription(),
                poi.getLatitude(),
                poi.getLongitude(),
                poi.getImageBase64(),
                poi.getCreatedAt(),
                poi.getUpdatedAt());
    }
}
