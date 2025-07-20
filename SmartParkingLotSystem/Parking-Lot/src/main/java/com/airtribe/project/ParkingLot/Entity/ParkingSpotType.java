package com.airtribe.project.ParkingLot.Entity;

public enum ParkingSpotType {
    TWO_WHEELER,
    REGULAR,
    LARGE;

    public static ParkingSpotType fromVehicleType(VehicleType vehicleType) {
        switch (vehicleType) {
            case CAR:
                return REGULAR;
            case BIKE:
                return TWO_WHEELER;
            case TRUCK:
                return LARGE;
            case BUS:
                return LARGE;
            case BICYCLE:
                return TWO_WHEELER;
            default:
                throw new IllegalArgumentException("Unknown vehicle type: " + vehicleType);
        }
    }
}
