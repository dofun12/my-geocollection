package com.geocollection.repository;

import com.geocollection.entity.PointOfInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Point of Interest entities.
 */
@Repository
public interface PointOfInterestRepository extends JpaRepository<PointOfInterest, Long> {

    List<PointOfInterest> findByUserId(Long userId);

    List<PointOfInterest> findByUserIdAndIsHomeTrue(Long userId);
}
