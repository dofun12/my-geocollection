package com.geocollection.service;

import com.geocollection.dto.CreatePoiRequest;
import com.geocollection.dto.PointOfInterestDTO;
import com.geocollection.dto.UpdatePoiRequest;
import com.geocollection.entity.PointOfInterest;
import com.geocollection.entity.User;
import com.geocollection.exception.ResourceNotFoundException;
import com.geocollection.repository.PointOfInterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing Points of Interest.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PointOfInterestService {

    private final PointOfInterestRepository repository;
    private final UserService userService;

    private static final int MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB in bytes
    private static final double EARTH_RADIUS_KM = 6371.0; // Earth's radius in kilometers

    /**
     * Create a new Point of Interest.
     */
    @Transactional
    public PointOfInterestDTO createPoi(CreatePoiRequest request, String username) {
        log.info("Creating new POI: {} for user: {}", request.getTitle(), username);

        // Validate image if present
        if (request.getImageBase64() != null && !request.getImageBase64().isEmpty()) {
            validateImage(request.getImageBase64());
        }

        User user = userService.findByUsername(username);

        // If setting as home, unset other home POIs for this user
        if (Boolean.TRUE.equals(request.getIsHome())) {
            unsetAllHomePois(user.getId());
        }

        PointOfInterest poi = new PointOfInterest();
        poi.setTitle(request.getTitle());
        poi.setDescription(request.getDescription());
        poi.setLatitude(request.getLatitude());
        poi.setLongitude(request.getLongitude());
        poi.setImageBase64(request.getImageBase64());
        poi.setUser(user);
        poi.setIsHome(request.getIsHome());

        PointOfInterest savedPoi = repository.save(poi);
        log.info("POI created successfully with ID: {}", savedPoi.getId());

        return convertToDTO(savedPoi, user.getId());
    }

    /**
     * Get all Points of Interest for a user.
     */
    @Transactional(readOnly = true)
    public List<PointOfInterestDTO> getAllPois(String username) {
        log.info("Fetching all POIs for user: {}", username);
        User user = userService.findByUsername(username);

        List<PointOfInterest> pois = repository.findByUserId(user.getId());
        log.info("Found {} POIs for user: {}", pois.size(), username);

        return pois.stream()
                .map(poi -> convertToDTO(poi, user.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Get a Point of Interest by ID.
     */
    @Transactional(readOnly = true)
    public PointOfInterestDTO getPoiById(Long id, String username) {
        log.info("Fetching POI with ID: {} for user: {}", id, username);
        User user = userService.findByUsername(username);

        PointOfInterest poi = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Point of Interest", id));

        // Verify ownership
        if (!poi.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Access denied: POI does not belong to user");
        }

        return convertToDTO(poi, user.getId());
    }

    /**
     * Update an existing Point of Interest.
     */
    @Transactional
    public PointOfInterestDTO updatePoi(Long id, UpdatePoiRequest request, String username) {
        log.info("Updating POI with ID: {} for user: {}", id, username);

        User user = userService.findByUsername(username);

        PointOfInterest poi = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Point of Interest", id));

        // Verify ownership
        if (!poi.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Access denied: POI does not belong to user");
        }

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

        return convertToDTO(updatedPoi, user.getId());
    }

    /**
     * Delete a Point of Interest.
     */
    @Transactional
    public void deletePoi(Long id, String username) {
        log.info("Deleting POI with ID: {} for user: {}", id, username);

        User user = userService.findByUsername(username);

        PointOfInterest poi = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Point of Interest", id));

        // Verify ownership
        if (!poi.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Access denied: POI does not belong to user");
        }

        repository.deleteById(id);
        log.info("POI deleted successfully with ID: {}", id);
    }

    /**
     * Set a POI as home for the user.
     */
    @Transactional
    public PointOfInterestDTO setAsHome(Long id, String username) {
        log.info("Setting POI with ID: {} as home for user: {}", id, username);

        User user = userService.findByUsername(username);

        PointOfInterest poi = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Point of Interest", id));

        // Verify ownership
        if (!poi.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Access denied: POI does not belong to user");
        }

        // Unset all other home POIs for this user
        unsetAllHomePois(user.getId());

        // Set this POI as home
        poi.setIsHome(true);
        PointOfInterest updatedPoi = repository.save(poi);

        log.info("POI set as home successfully with ID: {}", id);
        return convertToDTO(updatedPoi, user.getId());
    }

    /**
     * Unset a POI as home.
     */
    @Transactional
    public PointOfInterestDTO unsetAsHome(Long id, String username) {
        log.info("Unsetting POI with ID: {} as home for user: {}", id, username);

        User user = userService.findByUsername(username);

        PointOfInterest poi = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Point of Interest", id));

        // Verify ownership
        if (!poi.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Access denied: POI does not belong to user");
        }

        poi.setIsHome(false);
        PointOfInterest updatedPoi = repository.save(poi);

        log.info("POI unset as home successfully with ID: {}", id);
        return convertToDTO(updatedPoi, user.getId());
    }

    /**
     * Unset all home POIs for a user.
     */
    private void unsetAllHomePois(Long userId) {
        List<PointOfInterest> homePois = repository.findByUserIdAndIsHomeTrue(userId);
        homePois.forEach(poi -> poi.setIsHome(false));
        if (!homePois.isEmpty()) {
            repository.saveAll(homePois);
            log.info("Unset {} home POI(s) for user ID: {}", homePois.size(), userId);
        }
    }

    /**
     * Calculate distance between two coordinates using Haversine formula.
     * Returns distance in kilometers.
     */
    private double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1.doubleValue())) *
                        Math.cos(Math.toRadians(lat2.doubleValue())) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
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
     * Convert entity to DTO with distance calculation from home POI.
     */
    private PointOfInterestDTO convertToDTO(PointOfInterest poi, Long userId) {
        Double distanceFromHome = null;

        // Calculate distance from home POI if this is not the home POI
        if (!Boolean.TRUE.equals(poi.getIsHome())) {
            Optional<PointOfInterest> homePoi = repository.findByUserIdAndIsHomeTrue(userId).stream().findFirst();
            if (homePoi.isPresent()) {
                distanceFromHome = calculateDistance(
                        poi.getLatitude(),
                        poi.getLongitude(),
                        homePoi.get().getLatitude(),
                        homePoi.get().getLongitude());
            }
        }

        return new PointOfInterestDTO(
                poi.getId(),
                poi.getTitle(),
                poi.getDescription(),
                poi.getLatitude(),
                poi.getLongitude(),
                poi.getImageBase64(),
                poi.getIsHome(),
                distanceFromHome,
                poi.getCreatedAt(),
                poi.getUpdatedAt());
    }
}
