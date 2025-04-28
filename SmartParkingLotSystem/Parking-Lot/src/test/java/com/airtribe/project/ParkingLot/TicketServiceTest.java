package com.airtribe.project.ParkingLot;

import com.airtribe.project.ParkingLot.Entity.*;
import com.airtribe.project.ParkingLot.Repository.ParkingSpotRepository;
import com.airtribe.project.ParkingLot.Repository.TicketRepository;
import com.airtribe.project.ParkingLot.Repository.VehicleRepository;
import com.airtribe.project.ParkingLot.Service.TicketService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceTest {

    @InjectMocks
    private TicketService ticketService;

    @Mock
    private TicketRepository ticketRepo;

    @Mock
    private VehicleRepository vehicleRepo;

    @Mock
    private ParkingSpotRepository spotRepo;

    @Mock
    private Ticket ticket;

    @Test
    public void testCreateTicket() {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleNumber("XYZ123");
        vehicle.setVehicleType( VehicleType.CAR);

        ParkingSpot spot = new ParkingSpot();
        spot.setSpotNumber("A1");
        spot.setOccupied(false);

        EntryGate entryGate = new EntryGate();
        entryGate.setGateNumber("EntryGate 1");

        Ticket mockTicket = new Ticket();
        mockTicket.setVehicle(vehicle);
        mockTicket.setParkingSpot(spot);
        mockTicket.setEntryGate(entryGate);

        Mockito.when(ticketRepo.save(Mockito.any(Ticket.class))).thenReturn(mockTicket);

        Ticket createdTicket = ticketService.createTicket(vehicle, spot, entryGate);

        assertNotNull(createdTicket);
        assertEquals(createdTicket.getVehicle().getVehicleNumber(), "XYZ123");
    }

    @Test
    public void testCloseTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setEntryTime(LocalDateTime.now());
        ticket.setExitTime(LocalDateTime.now().plusHours(2));
        ticket.setFee(20.0);

        Mockito.when(ticketRepo.findById(Mockito.anyLong())).thenReturn(Optional.of(ticket));

        ExitGate exitGate = new ExitGate();
        exitGate.setGateNumber("ExitGate 1");

        ticketService.closeTicket(1L, exitGate);

        assertNotNull(ticket.getExitTime());
        assertTrue(ticket.getFee() > 0);
    }
}
