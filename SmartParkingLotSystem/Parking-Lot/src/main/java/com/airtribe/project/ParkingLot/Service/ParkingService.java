package com.airtribe.project.ParkingLot.Service;

import com.airtribe.project.ParkingLot.Entity.ParkingFloor;
import com.airtribe.project.ParkingLot.Entity.ParkingSpot;
import com.airtribe.project.ParkingLot.Entity.Vehicle;
import com.airtribe.project.ParkingLot.Exception.ParkingSpotUnavailableException;
import com.airtribe.project.ParkingLot.Exception.ResourceNotFoundException;
import com.airtribe.project.ParkingLot.Repository.ParkingFloorRepository;
import com.airtribe.project.ParkingLot.Repository.ParkingSpotRepository;
import com.airtribe.project.ParkingLot.Repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParkingService {
    @Autowired
    private ParkingSpotRepository spotRepo;
    @Autowired
    private VehicleRepository vehicleRepo;
    @Autowired
    private ParkingFloorRepository floorRepo;


    synchronized public ParkingSpot assignSpot(Vehicle vehicle) {
        List<ParkingSpot> available = spotRepo.findByIsOccupiedFalseAndParkingSpotType(vehicle.getVehicleType().getParkingSpotType().toString());
        if (available.isEmpty()) {
            throw new ParkingSpotUnavailableException("No available spot for vehicle type: " + vehicle.getVehicleType());
        }
        ParkingSpot spot = available.get(0);
        spot.setOccupied(true);
        spotRepo.save(spot);
        return spot;
    }

    public Map<String, Long> getParkingLotStatus() {
        long totalSpots = spotRepo.count();
        long occupiedSpots = spotRepo.countByIsOccupiedTrue();
        long availableSpots = totalSpots - occupiedSpots;

        Map<String, Long> status = new HashMap<>();
        status.put("totalSpots", totalSpots);
        status.put("occupiedSpots", occupiedSpots);
        status.put("availableSpots", availableSpots);

        return status;
    }

    public List<Map<String, Object>> getParkingFloorStatus() {
        List<Map<String, Object>> floorStatus = new ArrayList<>();

        List<ParkingFloor> floors = floorRepo.findAll();
        for (ParkingFloor floor : floors) {
            Map<String, Object> status = new HashMap<>();
            long totalSpots = spotRepo.countByFloor(floor);
            long availableSpots = spotRepo.countByIsOccupiedFalseAndFloor(floor);
            long occupiedSpots = totalSpots - availableSpots;

            status.put("floorNumber", floor.getFloorNumber());
            status.put("totalSpots", totalSpots);
            status.put("availableSpots", availableSpots);
            status.put("occupiedSpots", occupiedSpots);

            floorStatus.add(status);
        }
        return floorStatus;
    }

    public void updateSpotStatus(ParkingSpot spot, boolean isOccupied) {
        spot.setOccupied(isOccupied);
        spotRepo.save(spot);
    }

    public List<ParkingSpot> getAvailableSpots() {
        List<ParkingSpot> availableSpots = spotRepo.findByIsOccupiedFalse();
        if (availableSpots.isEmpty()) {
            throw new ParkingSpotUnavailableException("No available spots");
        }
        return availableSpots;
    }

    public List<ParkingSpot> getOccupiedSpots() {
        List<ParkingSpot> occupiedSpots = spotRepo.findByIsOccupiedTrue();
        if (occupiedSpots.isEmpty()) {
            throw new ParkingSpotUnavailableException("No occupied spots");
        }
        return occupiedSpots;
    }

    public Vehicle getVehicleById(Long vehicleId) {
        return vehicleRepo.findById(vehicleId).orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
    }
}
