package common;

/**
 * Slot dedicated for Truck parking.
 */
public class TruckSlot extends Slot {

    public TruckSlot(int floorNumber, int slotNumber) {
        super(floorNumber, slotNumber, Enums.SlotType.TRUCK);
    }

    @Override
    public boolean canAccommodate(Enums.VehicleType vehicleType) {
        return vehicleType == Enums.VehicleType.TRUCK;
    }
}
