import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Scheduler sjf = new PreemptiveSJFScheduler();
        Scheduler acs = new ACSScheduler();
        MatrixPrinter printer = new MatrixPrinter(System.out, true); // true = compact table like PDF

        System.out.println("Select mode:");
        System.out.println("1 - Test Case 1 (Assignment: Q=3, CS=0)");
        System.out.println("2 - Test Case 2 (Assignment: Q=3, CS=1)");
        System.out.println("3 - Test Case 3 (Report Mixed Workload: Q=3, CS=1)");
        System.out.println("4 - Custom Input");
        System.out.println("5 - Launch GUI");
        System.out.print("Choice: ");
        int choice = sc.nextInt();

        if (choice == 5) {
            sc.close();
            SchedulerGUI.launch();
            return;
        }

        List<Process> processes = new ArrayList<>();
        int quantum, cs;

        switch (choice) {
            case 1 -> {
                quantum = 3;
                cs = 0;
                processes.add(new Process(1, 0, 4, 2));
                processes.add(new Process(2, 1, 7, 3));
                processes.add(new Process(3, 3, 2, 1));
            }
            case 2 -> {
                quantum = 3;
                cs = 1;
                processes.add(new Process(1, 0, 5, 4));
                processes.add(new Process(2, 1, 6, 2));
                processes.add(new Process(3, 4, 4, 3));
            }
            case 3 -> {
                quantum = 3;
                cs = 1;
                processes.add(new Process(1, 0, 20, 4));
                processes.add(new Process(2, 1, 15, 5));
                processes.add(new Process(3, 4, 2, 1));
                processes.add(new Process(4, 8, 1, 2));
                processes.add(new Process(5, 12, 2, 1));
                processes.add(new Process(6, 25, 1, 3));
            }
            default -> {
                System.out.print("Enter number of processes: ");
                int n = sc.nextInt();
                System.out.print("Enter Quantum (Q): ");
                quantum = sc.nextInt();
                System.out.print("Enter Context Switch time (CS): ");
                cs = sc.nextInt();

                for (int i = 1; i <= n; i++) {
                    System.out.print("P" + i + " (Arrival Burst Priority): ");
                    int arrival = sc.nextInt();
                    int burst = sc.nextInt();
                    int priority = sc.nextInt();
                    if (arrival < 0 || burst <= 0 || priority < 1) {
                        throw new IllegalArgumentException(
                                "Invalid values (Arrival >= 0, Burst > 0, Priority >= 1).");
                    }
                    processes.add(new Process(i, arrival, burst, priority));
                }
            }
        }

        SimulationResult sjfResult = sjf.run(processes, quantum, cs);
        printer.print(sjfResult);

        SimulationResult acsResult = acs.run(processes, quantum, cs);
        printer.print(acsResult);

        sc.close();
    }
}