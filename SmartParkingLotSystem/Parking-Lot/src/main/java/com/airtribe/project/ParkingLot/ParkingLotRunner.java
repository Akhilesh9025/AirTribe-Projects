package com.airtribe.project.ParkingLot;

import com.airtribe.project.ParkingLot.Entity.*;
import com.airtribe.project.ParkingLot.Repository.*;
import com.airtribe.project.ParkingLot.Service.ParkingService;
import com.airtribe.project.ParkingLot.Service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class ParkingLotRunner implements CommandLineRunner {

    @Autowired
    private ParkingService parkingService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private VehicleRepository vehicleRepo;

    @Autowired
    private EntryGateRepository entryGateRepo;

    @Autowired
    private ExitGateRepository exitGateRepo;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚦 Multi-Vehicle Parking Lot Simulation Started...");

        // 1. Preload Entry and Exit Gate
        EntryGate entryGate = entryGateRepo.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No Entry Gates found"));

        ExitGate exitGate = exitGateRepo.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No Exit Gates found"));

        // 2. Simulate 5 Vehicles
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            int vehicleNumber = i;
            Thread t = new Thread(() -> {
                try {
                    Vehicle vehicle = new Vehicle();
                    vehicle.setVehicleNumber("XYZ-" + new Random().nextInt(9999));
                    vehicle.setVehicleType(VehicleType.values()[new Random().nextInt(VehicleType.values().length)]); // Random VehicleType
                    vehicleRepo.save(vehicle);

                    System.out.println("🚗 Vehicle Arrived: " + vehicle.getVehicleNumber());

                    ParkingSpot spot = parkingService.assignSpot(vehicle);
                    System.out.println("🅿️ Spot assigned to " + vehicle.getVehicleNumber() + ": SpotId " + spot.getId());

                    Ticket ticket = ticketService.createTicket(vehicle, spot, entryGate);
                    System.out.println("🎟️ Ticket issued: TicketId " + ticket.getId());

                    // Simulate some parking time
                    Thread.sleep(new Random().nextInt(5000) + 2000); // 2-7 seconds

                    Ticket closedTicket = ticketService.closeTicket(ticket.getId(), exitGate);
                    parkingService.updateSpotStatus(spot, false);

                    System.out.println("🚪 Vehicle Exited: " + vehicle.getVehicleNumber() +
                            ", Fee = ₹" + closedTicket.getFee());

                } catch (Exception e) {
                    System.out.println("❗ Error: " + e.getMessage());
                }
            });
            threads.add(t);
            t.start();
        }

        // 3. Wait for all vehicles to complete
        for (Thread t : threads) {
            t.join();
        }

        System.out.println("✅ Multi-Vehicle Simulation Complete");
    }
}
