package module4_payment;

import common.Enums;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service to issue, track, and validate monthly parking passes.
 * Demonstrates Service Layer and Single Responsibility Principle.
 */
public class MonthlyPassService {

    private final Map<String, MonthlyPass> passStore;
    private final AtomicInteger passCounter;

    public MonthlyPassService() {
        this.passStore = new LinkedHashMap<>();
        this.passCounter = new AtomicInteger(500);
    }

    public MonthlyPassService(Map<String, MonthlyPass> passStore) {
        this.passStore = passStore;
        this.passCounter = new AtomicInteger(500 + passStore.size());
    }

    public Map<String, MonthlyPass> getPassStore() {
        return passStore;
    }

    /**
     * Issues a new monthly pass for the specified vehicle.
     */
    public MonthlyPass issuePass(String registrationNumber, Enums.VehicleType vehicleType, LocalDate startDate, int months) {
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be null or empty.");
        }
        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null.");
        }
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (months <= 0) {
            months = 1;
        }

        LocalDate endDate = startDate.plusMonths(months).minusDays(1);
        String passId = "PASS-" + passCounter.incrementAndGet();

        MonthlyPass pass = new MonthlyPass(passId, registrationNumber, vehicleType, startDate, endDate);
        passStore.put(passId, pass);
        return pass;
    }

    /**
     * Finds the latest active pass for a vehicle registration number.
     */
    public MonthlyPass findValidPass(String registrationNumber, Enums.VehicleType vehicleType, LocalDate date) {
        if (registrationNumber == null || date == null) return null;
        String normalized = registrationNumber.trim().toUpperCase();
        for (MonthlyPass pass : passStore.values()) {
            if (pass.isValidFor(normalized, vehicleType, date)) {
                return pass;
            }
        }
        return null;
    }

    /**
     * Returns true if vehicle holds an active non-expired pass.
     */
    public boolean hasValidPass(String registrationNumber, Enums.VehicleType vehicleType, LocalDate date) {
        return findValidPass(registrationNumber, vehicleType, date) != null;
    }

    public MonthlyPass getPassById(String passId) {
        if (passId == null) return null;
        return passStore.get(passId.trim().toUpperCase());
    }

    public List<MonthlyPass> getAllPasses() {
        return new ArrayList<>(passStore.values());
    }
}
