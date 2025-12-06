package com.geocollection.controller;

import com.geocollection.dto.CreatePoiRequest;
import com.geocollection.dto.PointOfInterestDTO;
import com.geocollection.dto.UpdatePoiRequest;
import com.geocollection.service.PointOfInterestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Point of Interest operations.
 */
@RestController
@RequestMapping("/api/pois")
@RequiredArgsConstructor
@Slf4j
public class PointOfInterestController {

    private final PointOfInterestService service;

    /**
     * Create a new Point of Interest.
     * POST /api/pois
     */
    @PostMapping
    public ResponseEntity<PointOfInterestDTO> createPoi(@Valid @RequestBody CreatePoiRequest request) {
        String username = getAuthenticatedUsername();
        log.info("Received request to create POI: {} for user: {}", request.getTitle(), username);
        PointOfInterestDTO createdPoi = service.createPoi(request, username);
        return new ResponseEntity<>(createdPoi, HttpStatus.CREATED);
    }

    /**
     * Get all Points of Interest for the authenticated user.
     * GET /api/pois
     */
    @GetMapping
    public ResponseEntity<List<PointOfInterestDTO>> getAllPois() {
        String username = getAuthenticatedUsername();
        log.info("Received request to get all POIs for user: {}", username);
        List<PointOfInterestDTO> pois = service.getAllPois(username);
        return ResponseEntity.ok(pois);
    }

    /**
     * Get a Point of Interest by ID.
     * GET /api/pois/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PointOfInterestDTO> getPoiById(@PathVariable Long id) {
        String username = getAuthenticatedUsername();
        log.info("Received request to get POI with ID: {} for user: {}", id, username);
        PointOfInterestDTO poi = service.getPoiById(id, username);
        return ResponseEntity.ok(poi);
    }

    /**
     * Update an existing Point of Interest.
     * PUT /api/pois/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<PointOfInterestDTO> updatePoi(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePoiRequest request) {
        String username = getAuthenticatedUsername();
        log.info("Received request to update POI with ID: {} for user: {}", id, username);
        PointOfInterestDTO updatedPoi = service.updatePoi(id, request, username);
        return ResponseEntity.ok(updatedPoi);
    }

    /**
     * Delete a Point of Interest.
     * DELETE /api/pois/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePoi(@PathVariable Long id) {
        String username = getAuthenticatedUsername();
        log.info("Received request to delete POI with ID: {} for user: {}", id, username);
        service.deletePoi(id, username);
        return ResponseEntity.noContent().build();
    }

    /**
     * Set a POI as home.
     * POST /api/pois/{id}/set-home
     */
    @PostMapping("/{id}/set-home")
    public ResponseEntity<PointOfInterestDTO> setAsHome(@PathVariable Long id) {
        String username = getAuthenticatedUsername();
        log.info("Received request to set POI with ID: {} as home for user: {}", id, username);
        PointOfInterestDTO poi = service.setAsHome(id, username);
        return ResponseEntity.ok(poi);
    }

    /**
     * Unset a POI as home.
     * DELETE /api/pois/{id}/unset-home
     */
    @DeleteMapping("/{id}/unset-home")
    public ResponseEntity<PointOfInterestDTO> unsetAsHome(@PathVariable Long id) {
        String username = getAuthenticatedUsername();
        log.info("Received request to unset POI with ID: {} as home for user: {}", id, username);
        PointOfInterestDTO poi = service.unsetAsHome(id, username);
        return ResponseEntity.ok(poi);
    }

    /**
     * Get the authenticated username from Security Context.
     */
    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
