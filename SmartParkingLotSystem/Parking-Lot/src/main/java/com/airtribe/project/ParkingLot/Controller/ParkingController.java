package com.airtribe.project.ParkingLot.Controller;

import com.airtribe.project.ParkingLot.Entity.ParkingSpot;
import com.airtribe.project.ParkingLot.Entity.Vehicle;
import com.airtribe.project.ParkingLot.Service.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parking")
public class ParkingController {

    @Autowired
    private ParkingService parkingService;

    @PostMapping("/assign")
    public ResponseEntity<ParkingSpot> assignSpot(@RequestBody Vehicle vehicle) {
        ParkingSpot spot = parkingService.assignSpot(vehicle);
        return new ResponseEntity<>(spot, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateSpot(@RequestBody ParkingSpot spot) {
        parkingService.updateSpotStatus(spot, spot.isOccupied());
        return new ResponseEntity<>("Spot updated", HttpStatus.OK);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Long>> getParkingLotStatus() {
        Map<String, Long> status = parkingService.getParkingLotStatus();
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    @GetMapping("/floor-status")
    public ResponseEntity<List<Map<String, Object>>> getParkingFloorStatus() {
        List<Map<String, Object>> floorStatus = parkingService.getParkingFloorStatus();
        return new ResponseEntity<>(floorStatus, HttpStatus.OK);
    }

    @GetMapping("/available-spots")
    public ResponseEntity<List<ParkingSpot>> getAvailableSpots() {
        List<ParkingSpot> availableSpots = parkingService.getAvailableSpots();
        return new ResponseEntity<>(availableSpots, HttpStatus.OK);
    }

    @GetMapping("/occupied-spots")
    public ResponseEntity<List<ParkingSpot>> getOccupiedSpots() {
        List<ParkingSpot> occupiedSpots = parkingService.getOccupiedSpots();
        return new ResponseEntity<>(occupiedSpots, HttpStatus.OK);
    }
}
