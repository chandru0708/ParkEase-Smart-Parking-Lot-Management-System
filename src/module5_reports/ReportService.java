package module5_reports;

import common.*;
import module1_setup.Floor;
import module1_setup.ParkingLot;
import module4_payment.Payment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for administrative analytics, occupancy reporting, revenue audits, and safety alerts.
 * Demonstrates Single Responsibility and Analytical Reporting.
 */
public class ReportService {

    private final ParkingLot parkingLot;
    private final Map<String, Ticket> ticketStore;
    private final Map<String, Payment> paymentStore;

    public ReportService(ParkingLot parkingLot, Map<String, Ticket> ticketStore, Map<String, Payment> paymentStore) {
        this.parkingLot = parkingLot;
        this.ticketStore = ticketStore != null ? ticketStore : Collections.emptyMap();
        this.paymentStore = paymentStore != null ? paymentStore : Collections.emptyMap();
    }

    /**
     * Report 1: Slot status by Floor (Free, Occupied, Maintenance excluded from capacity).
     */
    public String getFloorWiseSlotReport() {
        if (parkingLot == null) return "No parking lot configured.";
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("                                 FLOOR-WISE SLOT OCCUPANCY REPORT                       \n");
        sb.append("========================================================================================\n");
        sb.append(String.format("%-10s | %-8s | %-12s | %-14s | %-18s | %-12s\n",
                "Floor #", "Status", "Free Slots", "Occupied Slots", "Under Maintenance", "Occupancy %".replace("%", "%%")));
        sb.append("----------------------------------------------------------------------------------------\n");

        for (Floor floor : parkingLot.getAllFloors()) {
            double occ = floor.getOccupancyPercentage();
            String alert = occ > 90.0 ? " [! ALERT: >90%]" : "";
            sb.append(String.format("%-10d | %-8s | %-12d | %-14d | %-18d | %6.1f%%%s\n",
                    floor.getFloorNumber(),
                    floor.isOpen() ? "OPEN" : "CLOSED",
                    floor.getFreeSlotsCount(),
                    floor.getOccupiedSlotsCount(),
                    floor.getUnderMaintenanceSlotsCount(),
                    occ, alert));
        }
        sb.append("========================================================================================\n");
        return sb.toString();
    }

    /**
     * Report 2: Slot status by Slot Type across all open floors.
     * Excludes UNDER_MAINTENANCE from Free and Occupied counts.
     */
    public String getSlotTypeWiseReport() {
        if (parkingLot == null) return "No parking lot configured.";
        Map<Enums.SlotType, Integer> freeMap = new EnumMap<>(Enums.SlotType.class);
        Map<Enums.SlotType, Integer> occupiedMap = new EnumMap<>(Enums.SlotType.class);
        Map<Enums.SlotType, Integer> maintMap = new EnumMap<>(Enums.SlotType.class);

        for (Enums.SlotType type : Enums.SlotType.values()) {
            freeMap.put(type, 0);
            occupiedMap.put(type, 0);
            maintMap.put(type, 0);
        }

        for (Floor floor : parkingLot.getAllFloors()) {
            for (Slot slot : floor.getAllSlots()) {
                Enums.SlotType type = slot.getSlotType();
                if (slot.getStatus() == Enums.SlotStatus.FREE) {
                    freeMap.put(type, freeMap.get(type) + 1);
                } else if (slot.getStatus() == Enums.SlotStatus.OCCUPIED) {
                    occupiedMap.put(type, occupiedMap.get(type) + 1);
                } else if (slot.getStatus() == Enums.SlotStatus.UNDER_MAINTENANCE) {
                    maintMap.put(type, maintMap.get(type) + 1);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append("                             SLOT TYPE OCCUPANCY REPORT                         \n");
        sb.append("================================================================================\n");
        sb.append(String.format("%-12s | %-12s | %-14s | %-18s\n", "Slot Type", "Free Slots", "Occupied Slots", "Under Maintenance"));
        sb.append("--------------------------------------------------------------------------------\n");
        for (Enums.SlotType type : Enums.SlotType.values()) {
            sb.append(String.format("%-12s | %-12d | %-14d | %-18d\n",
                    type, freeMap.get(type), occupiedMap.get(type), maintMap.get(type)));
        }
        sb.append("================================================================================\n");
        return sb.toString();
    }

    /**
     * Report 3: Total revenue for a given calendar day.
     * Considers only successful payments with paymentStatus == PAID.
     */
    public double getTotalRevenueForDate(LocalDate date) {
        if (date == null) return 0.0;
        double total = 0.0;
        for (Payment payment : paymentStore.values()) {
            if (payment.isSuccessful() && payment.getPaymentTimestamp().toLocalDate().equals(date)) {
                total += payment.getAmount();
            }
        }
        return total;
    }

    /**
     * Formats revenue statement for a given date.
     */
    public String getDailyRevenueReport(LocalDate date) {
        if (date == null) date = LocalDate.now();
        double revenue = getTotalRevenueForDate(date);
        int successfulCount = 0;
        int failedCount = 0;

        for (Payment payment : paymentStore.values()) {
            if (payment.getPaymentTimestamp().toLocalDate().equals(date)) {
                if (payment.isSuccessful()) successfulCount++;
                else failedCount++;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append(String.format("          DAILY REVENUE AUDIT [%s]         \n", date));
        sb.append("====================================================\n");
        sb.append(String.format(" Total Revenue Collected : Rs. %.2f\n", revenue));
        sb.append(String.format(" Successful Transactions : %d\n", successfulCount));
        sb.append(String.format(" Failed Transactions     : %d\n", failedCount));
        sb.append("====================================================\n");
        return sb.toString();
    }

    /**
     * Checks if any floor has occupancy exceeding 90%.
     */
    public List<String> checkFloorOccupancyAlerts() {
        List<String> alerts = new ArrayList<>();
        if (parkingLot == null) return alerts;
        for (Floor floor : parkingLot.getAllFloors()) {
            double occ = floor.getOccupancyPercentage();
            if (occ > 90.0) {
                alerts.add(String.format("[ADMIN ALERT] Floor %d occupancy is critical: %.1f%% (> 90.0%%)!",
                        floor.getFloorNumber(), occ));
            }
        }
        return alerts;
    }

    /**
     * Checks for vehicles parked for more than 24 hours continuously.
     */
    public List<String> check24HourParkingAlerts(LocalDateTime currentTime) {
        if (currentTime == null) currentTime = LocalDateTime.now();
        List<String> alerts = new ArrayList<>();
        for (Ticket ticket : ticketStore.values()) {
            if (ticket.isParkedOver24Hours(currentTime)) {
                long hours = ticket.getDurationMinutes(currentTime) / 60;
                alerts.add(String.format("[LONG STAY ALERT] Vehicle %s (%s) parked at Floor %d, Slot %d for %d hours continuously!",
                        ticket.getVehicleRegistrationNumber(), ticket.getVehicleType(),
                        ticket.getFloorNumber(), ticket.getSlotNumber(), hours));
            }
        }
        return alerts;
    }

    /**
     * Formats list of all currently parked vehicles.
     */
    public String getCurrentlyParkedVehiclesReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("                               CURRENTLY PARKED VEHICLES                                \n");
        sb.append("========================================================================================\n");
        sb.append(String.format("%-15s | %-8s | %-20s | %-12s | %-8s\n",
                "Registration", "Type", "Owner", "Floor/Slot", "Slot Type"));
        sb.append("----------------------------------------------------------------------------------------\n");

        int count = 0;
        if (parkingLot != null) {
            for (Floor floor : parkingLot.getAllFloors()) {
                for (Slot slot : floor.getAllSlots()) {
                    if (slot.isOccupied() && slot.getCurrentVehicle() != null) {
                        Vehicle v = slot.getCurrentVehicle();
                        sb.append(String.format("%-15s | %-8s | %-20s | Floor %d, Slot %-2d | %-8s\n",
                                v.getRegistrationNumber(), v.getVehicleType(), v.getOwnerName(),
                                slot.getFloorNumber(), slot.getSlotNumber(), slot.getSlotType()));
                        count++;
                    }
                }
            }
        }
        if (count == 0) {
            sb.append("                 No vehicles currently parked.\n");
        }
        sb.append("========================================================================================\n");
        sb.append(String.format("Total Parked Vehicles: %d\n", count));
        sb.append("========================================================================================\n");
        return sb.toString();
    }
}
