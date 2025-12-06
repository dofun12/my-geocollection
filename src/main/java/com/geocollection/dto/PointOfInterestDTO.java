package com.geocollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Point of Interest responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointOfInterestDTO {

    private Long id;
    private String title;
    private String description;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String imageBase64;
    private Boolean isHome;
    private Double distanceFromHome; // Distance in kilometers, null for home POI
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
