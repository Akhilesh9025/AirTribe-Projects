package com.airtribe.project.ParkingLot.Service;

import com.airtribe.project.ParkingLot.Entity.*;
import com.airtribe.project.ParkingLot.Exception.TicketAlreadyClosedException;
import com.airtribe.project.ParkingLot.Repository.ParkingSpotRepository;
import com.airtribe.project.ParkingLot.Repository.TicketRepository;
import com.airtribe.project.ParkingLot.Repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import java.time.Duration;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {
    @Autowired
    private TicketRepository ticketRepo;
    @Autowired
    private ParkingSpotRepository spotRepo;
    @Autowired
    private VehicleRepository vehicleRepo;


    public Ticket createTicket(Vehicle vehicle, ParkingSpot spot, EntryGate entryGate) {
        Ticket ticket = new Ticket();
        ticket.setVehicle(vehicle);
        ticket.setParkingSpot(spot);
        ticket.setEntryGate(entryGate);
        ticket.setEntryTime(LocalDateTime.now());
        return ticketRepo.save(ticket);
    }

    public double calculateFee(Ticket ticket) {
        Duration duration = Duration.between(ticket.getEntryTime(), ticket.getExitTime());
        return duration.toHours() * 10.0; // Example fee calculation
    }

    public Ticket closeTicket(Long ticketId, ExitGate exitGate) {
        Ticket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        if (ticket.getExitTime() != null) {
            throw new TicketAlreadyClosedException("Ticket already closed");
        }

        ticket.setExitGate(exitGate);
        ticket.setExitTime(LocalDateTime.now());
        ticket.setFee(calculateFee(ticket));
        ticketRepo.save(ticket);

        return ticket;
    }

    public Ticket getTicketById(Long ticketId) {
        return ticketRepo.findById(ticketId).orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
    }

    public List<Ticket> getTicketsByVehicle(Vehicle vehicle) {
        return ticketRepo.findByVehicle(vehicle);
    }

    public List<Ticket> getAllTickets() {
        return ticketRepo.findAll();
    }
}
