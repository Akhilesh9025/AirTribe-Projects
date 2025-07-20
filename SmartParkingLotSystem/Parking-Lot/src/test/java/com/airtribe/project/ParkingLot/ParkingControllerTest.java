package com.airtribe.project.ParkingLot;

import com.airtribe.project.ParkingLot.Controller.ParkingController;
import com.airtribe.project.ParkingLot.Entity.*;
import com.airtribe.project.ParkingLot.Service.ParkingService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(ParkingController.class)
public class ParkingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParkingService parkingService;

    @Test
    public void testAssignSpot() throws Exception {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleNumber("XYZ123");
        vehicle.setVehicleType(VehicleType.CAR);

        ParkingFloor floor = new ParkingFloor();
        floor.setFloorNumber(1);

        ParkingSpot spot = new ParkingSpot();
        spot.setSpotNumber("A1");
        spot.setParkingSpotType(ParkingSpotType.fromVehicleType(vehicle.getVehicleType()));
        spot.setOccupied(true);

        Mockito.when(parkingService.assignSpot(Mockito.any(Vehicle.class)))
                .thenReturn(spot);

        mockMvc.perform(post("/api/parking/assign?floorId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"licensePlate\": \"XYZ123\", \"vehicleType\": \"Car\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.spotNumber").value("A1"))
                .andExpect(jsonPath("$.occupied").value(true));
    }

    @Test
    public void testGetParkingLotStatus() throws Exception {
        Map<String, Long> status = new HashMap<>();
        status.put("totalSpots", 100L);
        status.put("availableSpots", 40L);
        status.put("occupiedSpots", 60L);

        Mockito.when(parkingService.getParkingLotStatus()).thenReturn(status);

        mockMvc.perform(get("/api/parking/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSpots").value(100))
                .andExpect(jsonPath("$.availableSpots").value(40))
                .andExpect(jsonPath("$.occupiedSpots").value(60));
    }
}
