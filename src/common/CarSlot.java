package common;

/**
 * Slot for Car parking. Can also accommodate a Bike when no Bike slot is free.
 */
public class CarSlot extends Slot {

    public CarSlot(int floorNumber, int slotNumber) {
        super(floorNumber, slotNumber, Enums.SlotType.CAR);
    }

    @Override
    public boolean canAccommodate(Enums.VehicleType vehicleType) {
        return vehicleType == Enums.VehicleType.CAR || vehicleType == Enums.VehicleType.BIKE;
    }
}
