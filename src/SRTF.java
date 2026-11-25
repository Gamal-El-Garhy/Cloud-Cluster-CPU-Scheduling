import java.util.*;
public class SRTF {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        int n = scan.nextInt();
        int cs = scan.nextInt();//cs-> context switch time

        if (n <= 0 || cs < 0) {
            System.out.println("Invalid input.");
            return;
        }

        Process[] processes = new Process[n];

        for (int i = 0; i < n; i++) {
            int arrivalTime = scan.nextInt();
            int burstTime = scan.nextInt();

            if (arrivalTime < 0 || burstTime <= 0) {
                System.out.println("Invalid arrival or burst time for process " + (i + 1));
                return;
            }

            processes[i] = new Process(i + 1, arrivalTime, burstTime);
        }

        // Sort processes by arrival time, then by ID for consistent ordering (to handle ties)
        Arrays.sort(processes, Comparator.comparingInt((Process p) -> p.arrivalTime).thenComparingInt(p -> p.id));
                                         
        int currentTime = 0, completed = 0;
        int prevIndex = -1;

        while (completed < n) {
            int index = -1;

            // Find the process with the shortest remaining time that has arrived
            for (int i = 0; i < n; i++) {
                if (processes[i].arrivalTime <= currentTime && processes[i].remainingTime > 0 &&
                    (index == -1 || processes[i].remainingTime < processes[index].remainingTime)) {
                    index = i;
                }
            }

            if (index != -1) {
                // Context switch if preemption happened
                if (prevIndex != -1 && prevIndex != index  && cs > 0) {
                    for (int c = 0; c < cs; c++) {
                        System.out.println("Time " + currentTime + ": Context Switch (loading P" + processes[index].id + ")");
                        currentTime++;
                    }
                    prevIndex = index;
                    continue;
                }

                // Record first start time for Response Time
                if (processes[index].firstStartTime == -1) {
                    processes[index].firstStartTime = currentTime;
                }

                System.out.println("Time " + currentTime + ": P" + processes[index].id + " running (Remaining: " + (processes[index].remainingTime - 1) + ")");

                processes[index].remainingTime--;
                currentTime++;
                prevIndex = index;

                if (processes[index].remainingTime == 0) {
                    processes[index].completionTime = currentTime;
                    processes[index].turnaroundTime = currentTime - processes[index].arrivalTime;
                    processes[index].waitingTime = processes[index].turnaroundTime - processes[index].burstTime;
                    processes[index].responseTime = processes[index].firstStartTime - processes[index].arrivalTime;
                    completed++;
                }
            } else {
                System.out.println("Time " + currentTime + ": IDLE");
                currentTime++;
                prevIndex = -1;
            }
        }

        // Sort back by ID for clean final output
        Arrays.sort(processes, Comparator.comparingInt(p -> p.id));

        double totalWT = 0, totalTAT = 0, totalRT = 0; //WT-> Waiting Time, TAT-> Turnaround Time, RT-> Response Time
        System.out.println("\n=== Results ===");
        for (Process p : processes) {
            totalWT += p.waitingTime;
            totalTAT += p.turnaroundTime;
            totalRT += p.responseTime;
            System.out.println("P" + p.id +" | CT: " + p.completionTime +" | WT: " + p.waitingTime +
                            " | TAT: " + p.turnaroundTime +" | RT: " + p.responseTime);
        }

        System.out.println("Avg WT: " + (totalWT / n));
        System.out.println("Avg TAT: " + (totalTAT / n));
        System.out.println("Avg RT: " + (totalRT / n));
    }
}