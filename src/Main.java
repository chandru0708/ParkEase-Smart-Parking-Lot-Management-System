import common.ParkingDataStore;
import service.ParkingLotService;
import ui.ConsoleMenu;

/**
 * Main application entry point for the Parking Lot Management System (ParkEase).
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("          WELCOME TO PARKEASE - PARKING LOT MANAGEMENT SYSTEM        ");
        System.out.println("                      Low-Level Design (LLD) Project                  ");
        System.out.println("======================================================================");

        // 1. Initialize Shared In-Memory DataStore
        ParkingDataStore dataStore = new ParkingDataStore();
        dataStore.seedInitialData();

        // 2. Initialize Unified Facade Service
        ParkingLotService parkingLotService = new ParkingLotService(dataStore);

        // 3. Launch Terminal Console User Interface
        ConsoleMenu consoleMenu = new ConsoleMenu(parkingLotService);
        consoleMenu.start();
    }
}
