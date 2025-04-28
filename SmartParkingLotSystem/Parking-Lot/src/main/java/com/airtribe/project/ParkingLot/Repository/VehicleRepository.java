package com.airtribe.project.ParkingLot.Repository;

import com.airtribe.project.ParkingLot.Entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

}