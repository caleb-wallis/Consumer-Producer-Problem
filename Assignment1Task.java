import java.util.ArrayList;

class Assignment1 {

    // Simulation Initialisation
    private static int NUM_MACHINES = 50; // Number of machines in the system that issue print requests
    private static int NUM_PRINTERS = 5; // Number of printers in the system that print requests
    private static int SIMULATION_TIME = 5;
    private static int MAX_PRINTER_SLEEP = 3;
    private static int MAX_MACHINE_SLEEP = 5;
    private static boolean sim_active = true;

    // Create an empty list of print requests
    printList list = new printList();

    public void startSimulation() {

        // ArrayList to keep for machine and printer threads
        ArrayList<Thread> mThreads = new ArrayList<Thread>();
        ArrayList<Thread> pThreads = new ArrayList<Thread>();

        // Create Machine and Printer threads
        for(int i = 1; i <= NUM_MACHINES; i++){
            MachineThread machine = new MachineThread(i);
            mThreads.add(machine);
        }
        //System.out.println("Created Machines");

        for(int i = 1; i <= NUM_PRINTERS; i++){
            PrinterThread printer = new PrinterThread(i);
            pThreads.add(printer);
        }
        //System.out.println("Created Printers");

        // start all the threads
        for(Thread machine : mThreads){
            machine.start();
        }
        //System.out.println("Started Machine Threads");

        for(Thread printer : pThreads){
            printer.start();
        }
        //System.out.println("Started Printer Threads");


        // let the simulation run for some time
        sleep(SIMULATION_TIME);

        // finish simulation
        sim_active = false;

        //System.out.println("Finish the Simulation");


        // Wait until all printer threads finish by using the join function
        try {
            for(Thread printer : pThreads){
                printer.join();
            }
        } catch (InterruptedException e) {
            System.out.println(e);
        }

        //System.out.println("Programs Done");

    }

    // Printer class
    public class PrinterThread extends Thread {
        private int printerID;

        public PrinterThread(int id) {
            printerID = id;
        }

        public void run() {
            while (sim_active) {
                // Simulate printer taking some time to print the document
                printerSleep();
                // Grab the request at the head of the queue and print it
                printDox(printerID);
            }
        }

        public void printerSleep() {
            int sleepSeconds = 1 + (int) (Math.random() * MAX_PRINTER_SLEEP);
            // sleep(sleepSeconds*1000);
            try {
                sleep(sleepSeconds * 1000);
            } catch (InterruptedException ex) {
                System.out.println("Sleep Interrupted");
            }
        }

        public void printDox(int printerID) {
            System.out.println("Printer ID:" + printerID + " : now available");
            // print from the queue
            list.queuePrint(list, printerID);
        }

    }

    // Machine class
    public class MachineThread extends Thread {
        private int machineID;

        public MachineThread(int id) {
            machineID = id;
        }

        public void run() {
            while (sim_active) {
                // machine sleeps for a random amount of time
                machineSleep();
                // machine wakes up and sends a print request
                printRequest(machineID);
            }
        }

        // machine sleeps for a random amount of time
        public void machineSleep() {
            int sleepSeconds = 1 + (int) (Math.random() * MAX_MACHINE_SLEEP);

            try {
                sleep(sleepSeconds * 1000);
            } catch (InterruptedException ex) {
                System.out.println("Sleep Interrupted");
            }
        }

        public void printRequest(int id) {
            System.out.println("Machine " + id + " Sent a print request");
            // Build a print document
            printDoc doc = new printDoc("My name is machine " + id, id);
            // Insert it in print queue
            list = list.queueInsert(list, doc);
        }
    }

    private static void sleep(int s) {
        try {
            Thread.sleep(s * 1000);
        } catch (InterruptedException ex) {
            System.out.println("Sleep Interrupted");
        }
    }
}
