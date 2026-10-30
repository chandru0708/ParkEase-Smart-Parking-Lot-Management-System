package common;

/**
 * Slot dedicated for Bike parking.
 */
public class BikeSlot extends Slot {

    public BikeSlot(int floorNumber, int slotNumber) {
        super(floorNumber, slotNumber, Enums.SlotType.BIKE);
    }

    @Override
    public boolean canAccommodate(Enums.VehicleType vehicleType) {
        return vehicleType == Enums.VehicleType.BIKE;
    }
}
