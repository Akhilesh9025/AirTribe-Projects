package com.airtribe.project.ParkingLot;

import com.airtribe.project.ParkingLot.Controller.TicketController;
import com.airtribe.project.ParkingLot.Entity.*;
import com.airtribe.project.ParkingLot.Repository.TicketRepository;
import com.airtribe.project.ParkingLot.Repository.VehicleRepository;
import com.airtribe.project.ParkingLot.Service.TicketService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(TicketController.class)
public class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private VehicleRepository vehicleRepo;

    @MockitoBean
    private TicketRepository ticketRepo;

    @Test
    public void testCreateTicket() throws Exception {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleNumber("XYZ123");

        ParkingSpot spot = new ParkingSpot();
        spot.setSpotNumber("A1");

        EntryGate entryGate = new EntryGate();
        entryGate.setGateNumber("Gate 1");

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setVehicle(vehicle);
        ticket.setParkingSpot(spot);
        ticket.setEntryGate(entryGate);

        Mockito.when(ticketService.createTicket(Mockito.any(Vehicle.class), Mockito.any(ParkingSpot.class), Mockito.any(EntryGate.class)))
                .thenReturn(ticket);

        mockMvc.perform(post("/api/tickets/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vehicle\": {\"licensePlate\": \"XYZ123\"}, \"spot\": {\"spotNumber\": \"A1\"}, \"entryGate\": {\"gateName\": \"Gate 1\"}}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vehicle.licensePlate").value("XYZ123"))
                .andExpect(jsonPath("$.spot.spotNumber").value("A1"));
    }

    @Test
    public void testGetTicketsByVehicle() throws Exception {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleNumber("XYZ123");

        Ticket ticket1 = new Ticket();
        ticket1.setId(1L);
        ticket1.setVehicle(vehicle);

        Ticket ticket2 = new Ticket();
        ticket2.setId(2L);
        ticket2.setVehicle(vehicle);

        List<Ticket> tickets = Arrays.asList(ticket1, ticket2);

        Mockito.when(vehicleRepo.findById(1L)).thenReturn(Optional.of(vehicle));
        Mockito.when(ticketService.getTicketsByVehicle(Mockito.any(Vehicle.class))).thenReturn(tickets);

        mockMvc.perform(get("/api/tickets/vehicle/{vehicleId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    public void testCloseTicket() throws Exception {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setExitTime(LocalDateTime.now());

        Mockito.when(ticketService.closeTicket(Mockito.anyLong(), Mockito.any(ExitGate.class)))
                .thenReturn(ticket);

        mockMvc.perform(put("/api/tickets/close/{ticketId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"exitGate\": {\"gateName\": \"ExitGate 1\"}}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Ticket closed"));
    }
}
