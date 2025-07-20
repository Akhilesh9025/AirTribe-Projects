package com.airtribe.project.ParkingLot.Controller;

import com.airtribe.project.ParkingLot.Entity.ExitGate;
import com.airtribe.project.ParkingLot.Entity.Ticket;
import com.airtribe.project.ParkingLot.Entity.Vehicle;
import com.airtribe.project.ParkingLot.Exception.ResourceNotFoundException;
import com.airtribe.project.ParkingLot.Repository.TicketRepository;
import com.airtribe.project.ParkingLot.Repository.VehicleRepository;
import com.airtribe.project.ParkingLot.Service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    @Autowired
    private TicketService ticketService;
    @Autowired
    private VehicleRepository vehicleRepo;
    @Autowired
    private TicketRepository ticketRepo;

    @PostMapping("/create")
    public ResponseEntity<Ticket> createTicket(@RequestBody Ticket ticket) {
        Ticket createdTicket = ticketService.createTicket(ticket.getVehicle(), ticket.getParkingSpot(), ticket.getEntryGate());
        return new ResponseEntity<>(createdTicket, HttpStatus.CREATED);
    }

    @PutMapping("/close/{ticketId}")
    public ResponseEntity<String> closeTicket(@PathVariable Long ticketId, @RequestBody ExitGate exitGate) {
        ticketService.closeTicket(ticketId, exitGate);
        return new ResponseEntity<>("Ticket closed", HttpStatus.OK);
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Long ticketId) {
        Ticket ticket = ticketService.getTicketById(ticketId);
        return new ResponseEntity<>(ticket, HttpStatus.OK);
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<Ticket>> getTicketsByVehicle(@PathVariable Long vehicleId) {
        Vehicle vehicle = vehicleRepo.findById(vehicleId).orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        List<Ticket> tickets = ticketService.getTicketsByVehicle(vehicle);
        return new ResponseEntity<>(tickets, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Ticket>> getAllTickets() {
        List<Ticket> tickets = ticketService.getAllTickets();
        return new ResponseEntity<>(tickets, HttpStatus.OK);
    }

}
