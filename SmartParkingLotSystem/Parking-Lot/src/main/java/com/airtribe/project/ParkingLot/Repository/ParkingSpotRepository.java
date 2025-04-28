package com.airtribe.project.ParkingLot.Repository;

import com.airtribe.project.ParkingLot.Entity.ParkingFloor;
import com.airtribe.project.ParkingLot.Entity.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
    List<ParkingSpot> findByIsOccupiedFalseAndVehicleType(String vehicleType);

    long countByIsOccupiedTrue();

    long countByFloor(ParkingFloor floor);

    long countByIsOccupiedFalseAndFloor(ParkingFloor floor);

    List<ParkingSpot> findByIsOccupiedFalse();

    List<ParkingSpot> findByIsOccupiedTrue();

    List<ParkingSpot> findByIsOccupiedFalseAndParkingSpotType(String string);
}