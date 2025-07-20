package com.airtribe.project.ParkingLot.Entity;

public enum VehicleType {
    CAR,
    BIKE,
    TRUCK,
    BUS,
    BICYCLE;

    public ParkingSpotType getParkingSpotType() {
        switch (this) {
            case CAR:
                return ParkingSpotType.REGULAR;
            case BIKE:
                return ParkingSpotType.TWO_WHEELER;
            case TRUCK:
                return ParkingSpotType.LARGE;
            case BUS:
                return ParkingSpotType.LARGE;
            case BICYCLE:
                return ParkingSpotType.TWO_WHEELER;
            default:
                throw new IllegalArgumentException("Unknown vehicle type: " + this);
        }
    }
}
