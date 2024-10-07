import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ResumableTaskExample {

    private static final int MAX_PARTS = 10;  // Simulating a task with 10 parts
    private static int currentPart = 0;       // Shared progress marker
    private static final Object lock = new Object();  // Lock for synchronization
    private static final int MAX_RETRIES = 3;

    public static void main(String[] args) {
        // Create a thread pool with 3 threads
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Submit the task to execute with retries
        for (int i = 0; i < MAX_RETRIES; i++) {
            executor.submit(() -> executeTaskWithRetries());
        }

        // Shutdown the executor when done
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    public static void executeTaskWithRetries() {
        while (true) {
            int partToExecute;

            // Synchronize access to shared progress marker
            synchronized (lock) {
                if (currentPart >= MAX_PARTS) {
                    System.out.println(Thread.currentThread().getName() + ": All parts completed.");
                    return;  // Task is fully completed
                }
                partToExecute = currentPart;  // Assign the part to be executed
                currentPart++;  // Increment progress marker
            }

            try {
                // Simulate task execution for this part
                System.out.println(Thread.currentThread().getName() + ": Executing part " + partToExecute);
                performTaskPart(partToExecute);
                System.out.println(Thread.currentThread().getName() + ": Completed part " + partToExecute);

                // Pass results to another thread (if applicable)
                // In this example, each thread will automatically pick the next part.
                
            } catch (Exception e) {
                // Handle task failure and retry if necessary
                System.err.println(Thread.currentThread().getName() + ": Failed to execute part " + partToExecute + ": " + e.getMessage());
                
                // Decrement currentPart to allow another thread to retry this part
                synchronized (lock) {
                    currentPart--;
                }

                // Retry logic or resubmit the task can be handled here
                break;  // For simplicity, we break here, but you can add retry logic if needed
            }
        }
    }

    public static void performTaskPart(int part) throws Exception {
        // Simulate a possible failure during task execution
        double random = Math.random();
        if (random < 0.3) {
            throw new Exception("Simulated failure at part " + part);
        }
        
        // Simulate successful task execution
        Thread.sleep(1000);  // Simulate time taken for the task
    }
}
