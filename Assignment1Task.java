import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

class Assignment1 {

    // Simulation Initialisation
    private static int NUM_MACHINES = 50; // Number of machines in the system that issue print requests
    private static int NUM_PRINTERS = 5; // Number of printers in the system that print requests
    private static int SIMULATION_TIME = 5;
    private static int MAX_PRINTER_SLEEP = 3;
    private static int MAX_MACHINE_SLEEP = 5;
    private static boolean sim_active = true;

    // Semaphore that will limit the queue size to NUM_PRINTERS
    private static Semaphore queueSpaceAvailable = new Semaphore(NUM_PRINTERS);
    // Semaphore to track messages available to print
    private static Semaphore documentsAvailable = new Semaphore(0);
    // Mutex lock that will only allow 1 device into the queue at once
    private static ReentrantLock deviceLock = new ReentrantLock();


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

        System.out.println("\nFINSIH THE SIMULATION\n");

        // Release enough permits to wake up all printer threads
        // This ensures that any printer waiting on documentsAvailable.acquire() will be unblocked and print the remaining messages in the queue
        for(int i = 0; i < NUM_PRINTERS; i++) {
            documentsAvailable.release();
        }

        // Release enough permits to wake up all machine threads
        // This ensures that any machine waiting on queueSpaceAvailable.acquire() will be unblocked and end
         for(int i = 0; i < NUM_MACHINES; i++) {
            queueSpaceAvailable.release();
        }

        // Wait until all printer and machine threads finish by using the join function
        try {
            for(Thread machine : mThreads){
                machine.join();
            }

            for(Thread printer : pThreads){
                printer.join();
            }
        } catch (InterruptedException e) {
            System.out.println("Error:" + e);
        }
        finally{
            System.out.println("\nPROGRAMS DONE\n");
        }
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
            try{
                // Wait until there's a document to print
                documentsAvailable.acquire();

                // Lock deviceLock if no other thread owns it / is interacting with the queue
                deviceLock.lock();
        
                System.out.println("Printer ID:" + printerID + " : now available");

                // print from the queue
                list.queuePrint(list, printerID);

                // Allow a machine to insert a new message
                queueSpaceAvailable.release(); 
            }
            catch(Exception e){
                System.out.println(e);
                // Release the documents available as we couldn't print from the queue
                documentsAvailable.release();
            }
            finally {
                // Unlock deviceLock to let other threads access the queue if we actually own the lock
                if (deviceLock.isHeldByCurrentThread()) {
                    deviceLock.unlock();
                }
            }
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
                try {
                    // machine sleeps for a random amount of time
                    machineSleep();

                    // machine wakes up and sends a print request
                    printRequest(machineID);
                } catch (Exception e) {
                    // Just exit silently when interrupted
                    break;
                }
            }
        }

        // machine sleeps for a random amount of time
        public void machineSleep() {
            int sleepSeconds = 1 + (int) (Math.random() * MAX_MACHINE_SLEEP);

            try {
                sleep(sleepSeconds * 1000);
            } catch (InterruptedException ex) {
              //  System.out.println("Sleep Interrupted");
            }
        }

        public void printRequest(int id) {
             try{  
                // Wait for space in queue
                queueSpaceAvailable.acquire();

                // Check if simulation is still active
                if (!sim_active) {return;}

                // Lock deviceLock if no other thread owns it / is interacting with the queue
                deviceLock.lock();
             
                System.out.println("Machine " + id + " Sent a print request");

                // Build a print document
                printDoc doc = new printDoc("My name is machine " + id, id);

                // Insert it in print queue
                list = list.queueInsert(list, doc);

                // Signal that a message is available to go into the queue
                documentsAvailable.release();
            }
            catch(Exception e){
                System.out.println(e);

                // Release the queue space as we couldn't add a message to the queue
                queueSpaceAvailable.release();
            }
            finally{
                // Unlock deviceLock to let other threads access the queue if we actually own the lock
                if (deviceLock.isHeldByCurrentThread()) {
                    deviceLock.unlock();
                }
            }
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
