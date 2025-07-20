package com.airtribe.project.ParkingLot;

import com.airtribe.project.ParkingLot.Entity.*;
import com.airtribe.project.ParkingLot.Repository.ParkingFloorRepository;
import com.airtribe.project.ParkingLot.Repository.ParkingSpotRepository;
import com.airtribe.project.ParkingLot.Repository.VehicleRepository;
import com.airtribe.project.ParkingLot.Service.ParkingService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ParkingServiceTest {

    @InjectMocks
    private ParkingService parkingService;

    @Mock
    private ParkingSpotRepository spotRepo;

    @Mock
    private VehicleRepository vehicleRepo;

    @Mock
    private ParkingFloorRepository floorRepo;

    @Test
    public void testAssignSpot() {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleNumber("XYZ123");
        vehicle.setVehicleType(VehicleType.CAR);

        ParkingFloor floor = new ParkingFloor();
        floor.setFloorNumber(1);

        ParkingSpot spot = new ParkingSpot();
        spot.setSpotNumber("A1");
        spot.setParkingSpotType(ParkingSpotType.fromVehicleType(vehicle.getVehicleType()));
        spot.setOccupied(false);
        spot.setParkingFloor(floor);

        Mockito.when(spotRepo.findByIsOccupiedFalseAndVehicleType(Mockito.anyString()))
                .thenReturn(Arrays.asList(spot));

        ParkingSpot assignedSpot = parkingService.assignSpot(vehicle);

        assertNotNull(assignedSpot);
        assertTrue(assignedSpot.isOccupied());
        assertEquals("A1", assignedSpot.getSpotNumber());
    }

    @Test
    public void testGetParkingLotStatus() {
        Map<String, Long> status = parkingService.getParkingLotStatus();

        assertNotNull(status);
        assertTrue(status.get("totalSpots") >= 0);
        assertTrue(status.get("availableSpots") >= 0);
    }
}
