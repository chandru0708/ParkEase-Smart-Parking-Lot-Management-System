package common;

import module1_setup.*;
import module4_payment.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Shared In-Memory Data Store holding all runtime collections across modules.
 * Demonstrates Single Source of Truth, avoiding duplicate or out-of-sync state.
 */
public class ParkingDataStore {

    private ParkingLot parkingLot;
    private final Map<String, Vehicle> vehicles;
    private final Map<String, Ticket> tickets;
    private final Map<String, Payment> payments;
    private final Map<String, MonthlyPass> monthlyPasses;
    private final List<Receipt> receipts;
    private Admin activeAdmin;

    public ParkingDataStore() {
        this.vehicles = new LinkedHashMap<>();
        this.tickets = new LinkedHashMap<>();
        this.payments = new LinkedHashMap<>();
        this.monthlyPasses = new LinkedHashMap<>();
        this.receipts = new ArrayList<>();
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    public void setParkingLot(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
    }

    public Map<String, Vehicle> getVehicles() {
        return vehicles;
    }

    public Map<String, Ticket> getTickets() {
        return tickets;
    }

    public Map<String, Payment> getPayments() {
        return payments;
    }

    public Map<String, MonthlyPass> getMonthlyPasses() {
        return monthlyPasses;
    }

    public List<Receipt> getReceipts() {
        return receipts;
    }

    public Admin getActiveAdmin() {
        return activeAdmin;
    }

    public void setActiveAdmin(Admin activeAdmin) {
        this.activeAdmin = activeAdmin;
    }

    /**
     * Finds active or paid ticket by vehicle registration number.
     */
    public Ticket findActiveTicketByRegistration(String regNumber) {
        if (regNumber == null) return null;
        String normalized = regNumber.trim().toUpperCase();
        for (Ticket t : tickets.values()) {
            if (t.getVehicleRegistrationNumber().equalsIgnoreCase(normalized) &&
                    (t.getStatus() == Enums.TicketStatus.ACTIVE ||
                     t.getStatus() == Enums.TicketStatus.PAID ||
                     t.getStatus() == Enums.TicketStatus.LOST)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Finds a valid monthly pass for vehicle on given date.
     */
    public MonthlyPass findValidMonthlyPass(String regNumber, Enums.VehicleType vehicleType, LocalDate date) {
        if (regNumber == null || date == null) return null;
        String normalized = regNumber.trim().toUpperCase();
        for (MonthlyPass pass : monthlyPasses.values()) {
            if (pass.isValidFor(normalized, vehicleType, date)) {
                return pass;
            }
        }
        return null;
    }

    /**
     * Seeds initial sample configuration.
     * At least 2 floors, Bike, Car, Truck slots, sample vehicles and monthly passes.
     */
    public void seedInitialData() {
        Admin admin = new Admin("ADM-001", "admin", "admin123", "Chief Administrator");
        this.activeAdmin = admin;

        ParkingLot lot = new ParkingLot("ParkEase Multi-Level Hub", "100 MG Road, Tech City");
        this.parkingLot = lot;

        // Floor 0: Ground Floor (2 Bike, 3 Car, 2 Truck slots)
        Floor f0 = new Floor(0);
        f0.addSlot(new BikeSlot(0, 1));
        f0.addSlot(new BikeSlot(0, 2));
        f0.addSlot(new CarSlot(0, 3));
        f0.addSlot(new CarSlot(0, 4));
        f0.addSlot(new CarSlot(0, 5));
        f0.addSlot(new TruckSlot(0, 6));
        f0.addSlot(new TruckSlot(0, 7));
        lot.addFloor(f0);

        // Floor 1: First Floor (3 Bike, 3 Car, 1 Truck slot)
        Floor f1 = new Floor(1);
        f1.addSlot(new BikeSlot(1, 1));
        f1.addSlot(new BikeSlot(1, 2));
        f1.addSlot(new BikeSlot(1, 3));
        f1.addSlot(new CarSlot(1, 4));
        f1.addSlot(new CarSlot(1, 5));
        f1.addSlot(new CarSlot(1, 6));
        f1.addSlot(new TruckSlot(1, 7));
        lot.addFloor(f1);

        // Sample Monthly Passes
        LocalDate now = LocalDate.now();
        MonthlyPass pass1 = new MonthlyPass("PASS-101", "KA01MP1111", Enums.VehicleType.CAR, now.minusDays(10), now.plusMonths(1));
        MonthlyPass pass2 = new MonthlyPass("PASS-102", "KA01BK2222", Enums.VehicleType.BIKE, now.minusDays(5), now.plusMonths(1));
        MonthlyPass passExpired = new MonthlyPass("PASS-103", "KA01CR9999", Enums.VehicleType.CAR, now.minusMonths(2), now.minusDays(1));

        monthlyPasses.put(pass1.getPassId(), pass1);
        monthlyPasses.put(pass2.getPassId(), pass2);
        monthlyPasses.put(passExpired.getPassId(), passExpired);

        // Sample registered vehicles
        Vehicle v1 = new Car("KA01MP1111", "Rohan Sharma");
        Vehicle v2 = new Bike("KA01BK2222", "Priya Verma");
        Vehicle v3 = new Truck("KA01TR3333", "FastLogistics Ltd");
        Vehicle v4 = new Car("KA01CR9999", "Amit Patel");

        vehicles.put(v1.getRegistrationNumber(), v1);
        vehicles.put(v2.getRegistrationNumber(), v2);
        vehicles.put(v3.getRegistrationNumber(), v3);
        vehicles.put(v4.getRegistrationNumber(), v4);
    }
}
