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
        log.info("Received request to create POI: {}", request.getTitle());
        PointOfInterestDTO createdPoi = service.createPoi(request);
        return new ResponseEntity<>(createdPoi, HttpStatus.CREATED);
    }

    /**
     * Get all Points of Interest.
     * GET /api/pois
     */
    @GetMapping
    public ResponseEntity<List<PointOfInterestDTO>> getAllPois() {
        log.info("Received request to get all POIs");
        List<PointOfInterestDTO> pois = service.getAllPois();
        return ResponseEntity.ok(pois);
    }

    /**
     * Get a Point of Interest by ID.
     * GET /api/pois/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PointOfInterestDTO> getPoiById(@PathVariable Long id) {
        log.info("Received request to get POI with ID: {}", id);
        PointOfInterestDTO poi = service.getPoiById(id);
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
        log.info("Received request to update POI with ID: {}", id);
        PointOfInterestDTO updatedPoi = service.updatePoi(id, request);
        return ResponseEntity.ok(updatedPoi);
    }

    /**
     * Delete a Point of Interest.
     * DELETE /api/pois/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePoi(@PathVariable Long id) {
        log.info("Received request to delete POI with ID: {}", id);
        service.deletePoi(id);
        return ResponseEntity.noContent().build();
    }
}
