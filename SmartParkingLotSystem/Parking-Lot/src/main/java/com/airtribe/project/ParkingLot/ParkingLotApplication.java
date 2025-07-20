package com.airtribe.project.ParkingLot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ParkingLotApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParkingLotApplication.class, args);

		ParkingLotRunner parkingLotRunner = new ParkingLotRunner();
		try {
			parkingLotRunner.run(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
