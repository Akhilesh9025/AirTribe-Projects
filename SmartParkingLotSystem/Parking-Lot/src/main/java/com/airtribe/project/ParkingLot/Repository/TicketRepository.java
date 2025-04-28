package com.airtribe.project.ParkingLot.Repository;

import com.airtribe.project.ParkingLot.Entity.Ticket;
import com.airtribe.project.ParkingLot.Entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByVehicle(Vehicle vehicle);
}
