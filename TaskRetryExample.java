import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TaskRetryExample {
    
    private static final int MAX_RETRIES = 3;

    public static void main(String[] args) {
        // Create a thread pool with 3 threads
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        // Submit the task
        executor.submit(() -> executeTaskWithRetries(MAX_RETRIES));
        
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

    public static void executeTaskWithRetries(int maxRetries) {
        int attempt = 1;
        boolean taskCompleted = false;
        
        while (attempt <= maxRetries && !taskCompleted) {
            try {
                System.out.println("Attempt #" + attempt + " to execute the task");
                
                // Simulate task execution
                performTask();
                
                // Mark task as completed
                taskCompleted = true;
                System.out.println("Task completed successfully on attempt #" + attempt);
                
            } catch (Exception e) {
                System.err.println("Task failed on attempt #" + attempt + ": " + e.getMessage());
                
                if (attempt == maxRetries) {
                    System.err.println("Max retries reached. Task failed.");
                }
            }
            
            attempt++;
        }
    }

    public static void performTask() throws Exception {
        // Simulate task failure by throwing an exception
        double random = Math.random();
        if (random < 0.7) {
            throw new Exception("Simulated task failure");
        }
        
        // Simulate successful task execution
        System.out.println("Task executed successfully");
    }
}


//=========================
/*
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledTaskRetryExample {
    
    private static final int MAX_RETRIES = 3;
    private static int attempt = 1;

    public static void main(String[] args) {
        // Create a ScheduledExecutorService with 3 threads
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
        
        // Submit the task for execution with retries
        retryTaskWithDelay(executor, 0, 2);
        
        // Shutdown the executor after some delay
        executor.schedule(() -> {
            executor.shutdown();
        }, 10, TimeUnit.SECONDS);
    }

    public static void retryTaskWithDelay(ScheduledExecutorService executor, int initialDelay, int retryDelay) {
        executor.schedule(() -> {
            try {
                System.out.println("Attempt #" + attempt + " to execute the task");
                
                // Simulate task execution
                performTask();
                
                // Mark task as completed
                System.out.println("Task completed successfully on attempt #" + attempt);
                
            } catch (Exception e) {
                System.err.println("Task failed on attempt #" + attempt + ": " + e.getMessage());
                attempt++;
                if (attempt <= MAX_RETRIES) {
                    retryTaskWithDelay(executor, retryDelay, retryDelay);
                } else {
                    System.err.println("Max retries reached. Task failed.");
                }
            }
        }, initialDelay, TimeUnit.SECONDS);
    }

    public static void performTask() throws Exception {
        // Simulate task failure by throwing an exception
        double random = Math.random();
        if (random < 0.7) {
            throw new Exception("Simulated task failure");
        }
        
        // Simulate successful task execution
        System.out.println("Task executed successfully");
    }
}
*/

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
